package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaPuntual;
import com.gamq.deportes.model.ReservaRecurrente;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.ReservaPuntualRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class NotificacionRecordatorioScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionRecordatorioScheduler.class);
    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm");

    private static final String HORAS_ANTICIPACION_RECORDATORIO_PAGO = "HORAS_ANTICIPACION_RECORDATORIO_PAGO";
    private static final String HORAS_ANTICIPACION_RECORDATORIO_RESERVA = "HORAS_ANTICIPACION_RECORDATORIO_RESERVA";
    private static final String MINUTOS_ANTICIPACION_CHECKIN = "MINUTOS_ANTICIPACION_CHECKIN";

    private static final List<String> ESTADOS_RESERVA_USO = Arrays.asList("PAGADA", "EN_CURSO");

    private final ConfiguracionSistemaRepository configuracionSistemaRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaPuntualRepository reservaPuntualRepository;
    private final ReservaRecurrenteRepository reservaRecurrenteRepository;
    private final NotificacionDestinatariosReservaService destinatariosService;
    private final NotificacionOperativaService notificacionOperativaService;

    public NotificacionRecordatorioScheduler(ConfiguracionSistemaRepository configuracionSistemaRepository,
                                             ReservaRepository reservaRepository,
                                             ReservaPuntualRepository reservaPuntualRepository,
                                             ReservaRecurrenteRepository reservaRecurrenteRepository,
                                             NotificacionDestinatariosReservaService destinatariosService,
                                             NotificacionOperativaService notificacionOperativaService) {
        this.configuracionSistemaRepository = configuracionSistemaRepository;
        this.reservaRepository = reservaRepository;
        this.reservaPuntualRepository = reservaPuntualRepository;
        this.reservaRecurrenteRepository = reservaRecurrenteRepository;
        this.destinatariosService = destinatariosService;
        this.notificacionOperativaService = notificacionOperativaService;
    }

    @Scheduled(fixedDelayString = "${gamq.notificaciones.recordatorios.fixed-delay-ms:60000}")
    @Transactional(readOnly = true)
    public void generarRecordatorios() {
        ejecutarAislado("pagos pendientes", this::generarRecordatoriosPagoPendiente);
        ejecutarAislado("reservas", this::generarRecordatoriosReserva);
        ejecutarAislado("check-in", this::generarRecordatoriosCheckin);
    }

    private void ejecutarAislado(String proceso, Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException ex) {
            logger.error("No se pudieron generar recordatorios de {}", proceso, ex);
            notificacionOperativaService.error("Recordatorios de " + proceso, ex.getMessage());
        }
    }

    private void generarRecordatoriosPagoPendiente() {
        Double horasAnticipacion = numericConfigActiva(HORAS_ANTICIPACION_RECORDATORIO_PAGO);
        if (horasAnticipacion == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (Reserva reserva : reservaRepository.findByEstadoReserva("PENDIENTE")) {
            if (!tieneContribuyente(reserva) || reserva.getNumeroTasa() == null || reserva.getFechaLimitePago() == null) {
                continue;
            }
            LocalDateTime vencePago = reserva.getFechaLimitePago();
            LocalDateTime recordatorioDesde = vencePago.minusMinutes(Math.round(horasAnticipacion * 60));
            if (now.isBefore(recordatorioDesde) || !now.isBefore(vencePago)) {
                continue;
            }
            crearRecordatorio(
                    reserva,
                    reserva.getUuid(),
                    HORAS_ANTICIPACION_RECORDATORIO_PAGO,
                    "Pago pendiente",
                    "Tu reserva " + safeValue(reserva.getNumeroReserva())
                            + " tiene un pago pendiente. Realiza el pago antes del "
                            + vencePago.format(FORMATO_FECHA_HORA) + "."
                            + detalleLugar(reserva),
                    "WARNING",
                    "{\"vencePago\":\"" + vencePago + "\"}");
        }
    }

    private void generarRecordatoriosReserva() {
        Double horasAnticipacion = numericConfigActiva(HORAS_ANTICIPACION_RECORDATORIO_RESERVA);
        if (horasAnticipacion == null) {
            return;
        }
        generarRecordatoriosUso(
                HORAS_ANTICIPACION_RECORDATORIO_RESERVA,
                Math.round(horasAnticipacion * 60),
                "Recordatorio de reserva",
                "Tu reserva %s inicia pronto.",
                "INFO");
    }

    private void generarRecordatoriosCheckin() {
        Double minutosAnticipacion = numericConfigActiva(MINUTOS_ANTICIPACION_CHECKIN);
        if (minutosAnticipacion == null) {
            return;
        }
        generarRecordatoriosUso(
                MINUTOS_ANTICIPACION_CHECKIN,
                Math.round(minutosAnticipacion),
                "Recordatorio de check-in",
                "Tu reserva %s está por iniciar. Recuerda realizar el check-in.",
                "INFO");
    }

    private void generarRecordatoriosUso(String tipo,
                                         long minutosAnticipacion,
                                         String titulo,
                                         String mensajeTemplate,
                                         String prioridad) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limite = now.plusMinutes(minutosAnticipacion);
        LocalDate fechaInicio = now.toLocalDate();
        LocalDate fechaFin = limite.toLocalDate();

        for (ReservaPuntual reservaPuntual : reservaPuntualRepository.findByFechaUsoBetween(fechaInicio, fechaFin)) {
            Reserva reserva = reservaPuntual.getReserva();
            if (!tieneContribuyente(reserva) || !ESTADOS_RESERVA_USO.contains(reserva.getEstadoReserva())) {
                continue;
            }
            if (reservaPuntual.getFechaUso() == null || reservaPuntual.getHoraInicio() == null) {
                continue;
            }
            LocalDateTime inicioUso = LocalDateTime.of(reservaPuntual.getFechaUso(), reservaPuntual.getHoraInicio());
            if (inicioUso.isBefore(now) || inicioUso.isAfter(limite)) {
                continue;
            }
            crearRecordatorio(
                    reserva,
                    reservaPuntual.getUuid(),
                    tipo,
                    titulo,
                    construirMensajeUso(reserva, reservaPuntual, tipo, mensajeTemplate),
                    prioridad,
                    "{\"inicioUso\":\"" + inicioUso + "\"}");
        }

        for (ReservaRecurrente recurrente :
                reservaRecurrenteRepository.findHabilitadasParaRecordatorio(fechaInicio, fechaFin)) {
            for (LocalDate fecha = max(fechaInicio, recurrente.getFechaInicio());
                 !fecha.isAfter(min(fechaFin, recurrente.getFechaFin()));
                 fecha = fecha.plusDays(1)) {
                for (String intervalo : intervalosDe(recurrente, fecha)) {
                    LocalTime horaInicio = horaInicio(intervalo);
                    if (horaInicio == null) continue;
                    LocalDateTime inicioUso = LocalDateTime.of(fecha, horaInicio);
                    if (inicioUso.isBefore(now) || inicioUso.isAfter(limite)) continue;

                    Reserva reserva = recurrente.getReserva();
                    String referencia = referenciaOcurrencia(recurrente, inicioUso);
                    crearRecordatorio(
                            reserva,
                            referencia,
                            tipo,
                            titulo,
                            construirMensajeRecurrente(reserva, inicioUso, tipo),
                            prioridad,
                            "{\"reservaRecurrenteUuid\":\"" + recurrente.getUuid()
                                    + "\",\"inicioUso\":\"" + inicioUso + "\"}");
                }
            }
        }
    }

    private String construirMensajeUso(Reserva reserva,
                                       ReservaPuntual reservaPuntual,
                                       String tipo,
                                       String mensajeTemplate) {
        String lugar = "";
        if (reserva.getCampoDeportivo() != null) {
            lugar = " en " + safeValue(reserva.getCampoDeportivo().getNombre());
            if (reserva.getCampoDeportivo().getComplejo() != null) {
                lugar += " del complejo " + safeValue(reserva.getCampoDeportivo().getComplejo().getNombre());
            }
        }
        String fechaHora = reservaPuntual.getFechaUso() + " a las " + reservaPuntual.getHoraInicio();
        if (HORAS_ANTICIPACION_RECORDATORIO_RESERVA.equals(tipo)) {
            return "Tu reserva " + safeValue(reserva.getNumeroReserva()) + lugar
                    + " inicia el " + fechaHora + ".";
        }
        return String.format(mensajeTemplate, safeValue(reserva.getNumeroReserva()))
                + lugar + " Inicio: " + fechaHora + ".";
    }

    private String construirMensajeRecurrente(Reserva reserva,
                                               LocalDateTime inicioUso,
                                               String tipo) {
        String lugar = detalleLugar(reserva);
        if (HORAS_ANTICIPACION_RECORDATORIO_RESERVA.equals(tipo)) {
            return "Tu sesión de la reserva recurrente " + safeValue(reserva.getNumeroReserva())
                    + " inicia el " + inicioUso.format(FORMATO_FECHA_HORA) + "." + lugar;
        }
        return "Tu sesión de la reserva recurrente " + safeValue(reserva.getNumeroReserva())
                + " está por iniciar. Recuerda realizar el check-in. Inicio: "
                + inicioUso.format(FORMATO_FECHA_HORA) + "." + lugar;
    }

    private List<String> intervalosDe(ReservaRecurrente recurrente, LocalDate fecha) {
        if (recurrente.getTablero() == null || recurrente.getTablero().getHorario() == null) {
            return List.of();
        }
        String[][][] horario = recurrente.getTablero().getHorario();
        int mes = (int) java.time.temporal.ChronoUnit.MONTHS.between(
                YearMonth.from(recurrente.getFechaInicio()), YearMonth.from(fecha));
        int dia = fecha.getDayOfMonth() - 1;
        if (mes < 0 || mes >= horario.length
                || horario[mes] == null || dia < 0 || dia >= horario[mes].length
                || horario[mes][dia] == null) {
            return List.of();
        }
        return Arrays.stream(horario[mes][dia])
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    private LocalTime horaInicio(String intervalo) {
        if (intervalo == null || !intervalo.contains("-")) return null;
        try {
            return LocalTime.parse(intervalo.substring(0, intervalo.indexOf('-')).trim());
        } catch (RuntimeException ex) {
            logger.warn("Horario recurrente inválido: {}", intervalo);
            return null;
        }
    }

    private String referenciaOcurrencia(ReservaRecurrente recurrente, LocalDateTime inicio) {
        return UUID.nameUUIDFromBytes(
                (recurrente.getUuid() + "|" + inicio).getBytes(StandardCharsets.UTF_8))
                .toString();
    }

    private LocalDate max(LocalDate izquierda, LocalDate derecha) {
        return derecha != null && derecha.isAfter(izquierda) ? derecha : izquierda;
    }

    private LocalDate min(LocalDate izquierda, LocalDate derecha) {
        return derecha != null && derecha.isBefore(izquierda) ? derecha : izquierda;
    }

    private String detalleLugar(Reserva reserva) {
        if (reserva.getCampoDeportivo() == null) return "";
        StringBuilder lugar = new StringBuilder(" Lugar: ")
                .append(safeValue(reserva.getCampoDeportivo().getNombre()));
        if (reserva.getCampoDeportivo().getComplejo() != null) {
            lugar.append(", complejo ")
                    .append(safeValue(reserva.getCampoDeportivo().getComplejo().getNombre()));
        }
        return lugar.append(".").toString();
    }

    private void crearRecordatorio(Reserva reserva,
                                   String referenciaUuid,
                                   String tipo,
                                   String titulo,
                                   String mensaje,
                                   String prioridad,
                                   String metadataJson) {
        String mensajeComun = mensaje
                .replaceFirst("^Tu reserva", "La reserva")
                .replaceFirst("^Tu sesión", "La sesión");
        NotificacionDto comun = new NotificacionDto()
                .setTipo(tipo)
                .setTitulo(ReservaNotificacionFormatter.titulo(titulo, reserva))
                .setMensaje(ReservaNotificacionFormatter.mensaje(reserva, mensajeComun))
                .setPrioridad(prioridad)
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(referenciaUuid)
                .setAccionUrl(ReservaNotificacionFormatter.ruta(reserva))
                .setMetadataJson(metadataJson);
        destinatariosService.publicar(tipo, reserva, comun, comun, null);
    }

    private String obtenerRutaReserva(Reserva reserva) {
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/recurrent";
        }
        if ("periodo".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/schools";
        }
        return "/reservations/one-time";
    }

    private Double numericConfigActiva(String clave) {
        Optional<ConfiguracionSistema> config = configuracionSistemaRepository
                .findNotificationConfigsByClave(clave).stream().findFirst();
        if (config.isEmpty()
                || !config.get().isConfiguracionActiva()) {
            return null;
        }
        Double value = config.get().getValorComoDouble();
        return value != null && value > 0 ? value : null;
    }

    private boolean tieneContribuyente(Reserva reserva) {
        return reserva != null
                && reserva.getContribuyente() != null
                && reserva.getContribuyente().getUuid() != null;
    }

    private String mensajeInterno(Reserva reserva, String mensajeContribuyente) {
        String contribuyente = reserva.getContribuyente() == null
                ? "el contribuyente"
                : safeValue(reserva.getContribuyente().getNombreCompleto());
        if (mensajeContribuyente.startsWith("Tu reserva")) {
            return mensajeContribuyente.replaceFirst(
                    "Tu reserva",
                    "La reserva de " + contribuyente);
        }
        if (mensajeContribuyente.startsWith("Tu sesión")) {
            return mensajeContribuyente.replaceFirst(
                    "Tu sesión",
                    "La sesión de " + contribuyente);
        }
        return mensajeContribuyente + " Contribuyente: " + contribuyente + ".";
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }
}
