package com.gamq.deportes.service.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamq.deportes.dto.ReservaRevisionPage;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.mapper.ReservaMapper;
import com.gamq.deportes.dto.ReservaRevisionPage.Resumen;
import com.gamq.deportes.dto.ReservaRevisionPage.Resultado;
import com.gamq.deportes.model.*;
import com.gamq.deportes.model.AnularTasa.AnularTasaRequest;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Pagos.PagoRequest;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import javax.persistence.criteria.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/** Shared by list requests and the disabled scheduler. Each row commits independently. */
@Service
public class ReservaRuatRevisionService {
    private static final Logger log = LoggerFactory.getLogger(ReservaRuatRevisionService.class);
    private static final ZoneId ZONE = ZoneId.of("America/La_Paz");
    private static final Set<String> TERMINALES = Set.of("CANCELADA", "NO_SHOW", "COMPLETADA", "EXPIRADA");
    private static final String MOTIVO_EXPIRACION = "NO SE EFECTUO EL PAGO EN EL DIA DE REGISTRO";
    private static final Set<String> MENSAJES_SIN_PAGO = Set.of(
            "No Existe el registro de la tasa y otro ingreso.",
            "La Tasa y Otro Ingreso no tiene pagos realizados.");
    private static final DateTimeFormatter FECHA_RUAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private final ApiService ruat;
    private final ObjectMapper mapper;
    private final ReservaRepository reservas;
    private final PagoRepository pagos;
    private final ReservaAuditoriaRepository auditorias;
    private final ReservaNotificacionService notificaciones;
    private final PagoNotificacionService pagoNotificaciones;
    private final EntityManager entityManager;
    private final TransactionTemplate transaction;
    private final Clock clock;

    @Autowired
    private com.gamq.deportes.service.SistemaAuditoriaService auditoriaService;

    @Autowired
    public ReservaRuatRevisionService(ApiService ruat, ObjectMapper mapper, ReservaRepository reservas,
            PagoRepository pagos, ReservaAuditoriaRepository auditorias,
            ReservaNotificacionService notificaciones, PagoNotificacionService pagoNotificaciones,
            EntityManager entityManager, PlatformTransactionManager transactionManager) {
        this(ruat, mapper, reservas, pagos, auditorias, notificaciones, pagoNotificaciones,
                entityManager, transactionManager, Clock.system(ZONE));
    }

    ReservaRuatRevisionService(ApiService ruat, ObjectMapper mapper, ReservaRepository reservas,
            PagoRepository pagos, ReservaAuditoriaRepository auditorias,
            ReservaNotificacionService notificaciones, PagoNotificacionService pagoNotificaciones,
            EntityManager entityManager, PlatformTransactionManager transactionManager, Clock clock) {
        this.ruat = ruat;
        this.mapper = mapper;
        this.reservas = reservas;
        this.pagos = pagos;
        this.auditorias = auditorias;
        this.notificaciones = notificaciones;
        this.pagoNotificaciones = pagoNotificaciones;
        this.entityManager = entityManager;
        this.clock = clock.withZone(ZONE);
        this.transaction = new TransactionTemplate(transactionManager);
        this.transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /** Bounded caller (the disabled scheduler). List endpoints use revisarLista. */
    public <T, D> Page<D> revisarPagina(Supplier<Page<T>> query, Function<T, Reserva> root,
                                       Function<T, D> toDto) {
        return revisarLista(query, root, toDto, after -> after == 0 ? query.get() : Page.empty());
    }

    /** Ownership/type scope only, independent of UI filters and page.
     * Keyset traversal avoids skipping rows when their states change during review. */
    public static <T> Specification<T> candidatas(Specification<T> scope, String relation, long after) {
        return scope.and((root, query, cb) -> {
            Path<?> reserva = relation.isEmpty() ? root : root.get(relation);
            return cb.and(cb.greaterThan(reserva.get("idReserva"), after),
                    reserva.get("estadoReserva").in("SOLICITADA", "PENDIENTE", "PAGADA", "EN_CURSO"));
        });
    }

    public <T, D> Page<D> revisarLista(Supplier<Page<T>> query, Function<T, Reserva> root,
                                      Function<T, D> toDto, Function<Long, Page<T>> candidates) {
        Resumen summary = new Resumen();
        Set<String> seen = new HashSet<>();
        boolean remoteAvailable = true;
        long after = 0;
        while (true) {
            final long cursor = after;
            List<ReservaDto> batch = transaction.execute(status -> {
                entityManager.clear();
                return candidates.apply(cursor).map(item -> ReservaMapper.toDto(root.apply(item))).getContent();
            });
            if (batch.isEmpty()) break;
            for (ReservaDto snapshot : batch) {
                after = Math.max(after, snapshot.getIdReserva());
                String id = snapshot.getUuid();
                if (id == null || !seen.add(id)) continue;
                String deferred = !remoteAvailable ? "Consulta pendiente por indisponibilidad del servicio externo." : null;
                // Terminal states and calendar-only transitions do not depend on RUAT.
                Resultado result = revisarUuid(snapshot, deferred);
                if ("NO_DISPONIBLE".equals(result.getRuat())) remoteAvailable = false;
                summary.getResultados().add(result);
            }
            if (after <= cursor) throw new IllegalStateException("El cursor de revisión no avanzó.");
        }
        // Map lazy relationships inside a transaction, also without OpenEntityManagerInView.
        return transaction.execute(status -> {
            entityManager.clear();
            return new ReservaRevisionPage<>(query.get().map(toDto), summary);
        });
    }

    public <T, D> Page<D> leerPagina(Supplier<Page<T>> query, Function<T, D> toDto) {
        return transaction.execute(status -> query.get().map(toDto));
    }

    private Resultado revisarUuid(ReservaDto snapshot, String deferred) {
        String uuid = snapshot.getUuid();
        Resultado attempt = new Resultado();
        try {
            return transaction.execute(status -> {
                Reserva reserva = reservas.findByUuidForRevision(uuid).orElse(null);
                if (reserva == null) return attempt.setReservaDto(snapshot).setEstadoAnterior(snapshot.getEstadoReserva())
                        .setResultado("OMITIDA").setDetalleLocal("La reserva ya no está disponible.");
                // Another list request may already have committed this row.
                entityManager.refresh(reserva);
                return revisarEstado(reserva, deferred, attempt);
            });
        } catch (RuntimeException ex) {
            log.warn("No se confirmó la revisión de la reserva {}: {}", uuid, ex.getClass().getSimpleName());
            // Keep any received RUAT response even if the local transaction rolls back.
            return attempt.setReservaDto(snapshot).setEstadoAnterior(snapshot.getEstadoReserva()).setResultado("ERROR")
                    .setDetalleLocal("No se confirmó el cambio local; revise RUAT antes de reintentar.");
        }
    }

    // Package-visible for regression tests; production uses revisarUuid's transaction.
    Resultado revisarEstado(Reserva reserva) {
        return revisarEstado(reserva, null, new Resultado());
    }

    private Resultado revisarEstado(Reserva reserva, String deferred, Resultado result) {
        String anterior = normal(reserva.getEstadoReserva());
        result.setReservaDto(ReservaMapper.toDto(reserva)).setEstadoAnterior(reserva.getEstadoReserva())
                .setMotivo("No corresponde un cambio de estado.");
        if (TERMINALES.contains(anterior)) return result.setMotivo("Estado terminal; no se modifica.");

        boolean periodo = "periodo".equalsIgnoreCase(reserva.getTipoReserva());
        LocalDateTime now = LocalDateTime.now(clock);
        if ("SOLICITADA".equals(anterior)) {
            if (!periodo && "RECHAZADA".equalsIgnoreCase(reserva.getEstadoValidacion())) {
                if (hasText(reserva.getNumeroTasa())) return result
                        .setMotivo("Solicitud rechazada con tasa: requiere anulación explícita confirmada por RUAT.");
                return cambiar(reserva, result, "CANCELADA", "Solicitud rechazada administrativamente.", now);
            }
            return result;
        }

        // Preserve calendar-only progress of already paid bookings: no RUAT
        // mutation, no annulment, and no regression to PAGADA.
        if ("PAGADA".equals(anterior) || "EN_CURSO".equals(anterior)) {
            if (periodo) {
                // Monthly fees represent student payments, not field occupancy.
                // Calendar changes never complete them or undo confirmed payments.
                return result.setMotivo("La mensualidad conserva su estado; no se completa por fechas.");
            }
            if (finalizada(reserva, now)) return cambiar(reserva, result, "COMPLETADA", "El período de uso terminó.", now);
            if ("PAGADA".equals(anterior) && enCurso(reserva, now)) {
                return cambiar(reserva, result, "EN_CURSO", "Fecha/día u horario de uso en curso.", now);
            }
            return result;
        }
        if (!"PENDIENTE".equals(anterior)) return result;
        if (!periodo && Boolean.TRUE.equals(reserva.getEsReservaGratuita())) {
            if ("APROBADA".equalsIgnoreCase(reserva.getEstadoValidacion())) {
                if (finalizada(reserva, now)) return cambiar(reserva, result, "COMPLETADA", "Reserva gratuita aprobada: uso finalizado.", now);
                if (enCurso(reserva, now)) return cambiar(reserva, result, "EN_CURSO", "Reserva gratuita aprobada: uso en curso.", now);
            }
            return result;
        }
        if (!hasText(reserva.getNumeroTasa())) return result.setMotivo("Sin número de tasa; no se consulta ni expira.");
        if (deferred != null) return result.setResultado("OMITIDA").setRuat("NO_CONSULTADO")
                .setMotivo(null).setDetalleLocal(deferred);

        // Query payment regardless of deadline and automatic-cancellation flag.
        PagoRequest request = new PagoRequest();
        request.setNumeroTasa(reserva.getNumeroTasa());
        PagoToken token = new PagoToken();
        token.setPago(request);
        ResponseEntity<byte[]> response;
        try {
            response = ruat.consultaPagoRevision(token);
        } catch (RuntimeException ex) {
            return sinRespuesta(result, ex);
        }
        JsonNode body = registrarRespuesta(result, response);
        if (!consultaSinPago(response, body) && !confirmada(response, body))
            return respuestaNoAceptada(result, response);
        JsonNode data = body.hasNonNull("datos") ? body.get("datos") : body;
        if (!data.isObject() || !data.has("pagoTasa")) return result.setResultado("ERROR")
                .setRuat("RESPUESTA_INVALIDA").setDetalleLocal("La respuesta no contiene el campo pagoTasa.");
        JsonNode payment = data.get("pagoTasa");
        if (payment != null && !payment.isNull()) {
            Pago nuevoPago;
            try {
                nuevoPago = pagoConfirmado(reserva, payment, body, now);
            } catch (RuntimeException ex) {
                return result.setResultado("ERROR").setRuat("NO_CONFIRMADO")
                        .setDetalleLocal("Los datos recibidos no permiten registrar un pago válido para esta tasa.");
            }
            // The payment save and state change share the same transaction.
            result.setRuat("PAGO_CONFIRMADO");
            registrarPago(nuevoPago);
            String nuevo = periodo ? "PAGADA"
                    : finalizada(reserva, now) ? "COMPLETADA" : enCurso(reserva, now) ? "EN_CURSO" : "PAGADA";
            return cambiar(reserva, result, nuevo, periodo ? "Pago de mensualidad confirmado por RUAT."
                    : "Pago confirmado por RUAT; estado calculado según las fechas de uso.", now);
        }
        result.setRuat("SIN_PAGO");
        // Even incorrect legacy flags/deadlines must never expire a monthly fee.
        if (periodo) return result;
        if (reserva.getPagosList().stream().anyMatch(p ->
                "PAGADO".equalsIgnoreCase(p.getEstadoPago()) || "CONFIRMADO".equalsIgnoreCase(p.getEstadoPago()))) {
            return result.setDetalleLocal("Existe un pago local confirmado; no se solicita anulación.");
        }
        if (!Boolean.TRUE.equals(reserva.getCancelacionAutomatica())) return result;
        if (reserva.getFechaLimitePago() == null || !now.isAfter(reserva.getFechaLimitePago())) {
            return result;
        }
        AnularTasaRequest annul = new AnularTasaRequest();
        annul.setNumeroTasa(reserva.getNumeroTasa());
        annul.setMotivoTasa(MOTIVO_EXPIRACION);
        annul.setObservacion("ANULACION POR VENCIMIENTO DEL PLAZO DE PAGO");
        AnularTasaToken annulToken = new AnularTasaToken();
        annulToken.setAnular(annul);
        try {
            response = ruat.anularTasaRevision(annulToken);
        } catch (RuntimeException ex) {
            return sinRespuesta(result, ex);
        }
        body = registrarRespuesta(result, response);
        // The no-payment business response never authorizes an annulment.
        if (!confirmada(response, body)) return respuestaNoAceptada(result, response);
        result.setRuat("ANULACION_CONFIRMADA");
        reserva.setMotivoRechazo(MOTIVO_EXPIRACION).setFechaRechazo(now).setAnuladoPor(null);
        return cambiar(reserva, result, "EXPIRADA", "Plazo vencido y anulación confirmada por RUAT.", now);
    }

    private Resultado cambiar(Reserva reserva, Resultado result,
                                              String estado, String motivo, LocalDateTime now) {
        return cambiar(reserva, result, estado, motivo, now, "REVISION_AUTOMATICA");
    }

    private Resultado cambiar(Reserva reserva, Resultado result, String estado, String motivo,
                              LocalDateTime now, String origen) {
        reserva.setEstadoReserva(estado).setFechaActualizacion(now);
        reservas.save(reserva);
        auditorias.save(new ReservaAuditoria().setReserva(reserva)
                .setAccion("EXPIRADA".equals(estado) ? "EXPIRE" : "UPDATE_STATUS")
                .setEstadoAnterior(result.getEstadoAnterior()).setEstadoNuevo(estado)
                .setDatosCambio(datosAuditoria(reserva, result, origen))
                .setObservaciones(motivo + " Origen: " + origen));
        if ("EXPIRADA".equals(estado)) notificaciones.notificarExpirada(reserva);
        if ("CANCELADA".equals(estado)) notificaciones.notificarCancelada(reserva);
        if (result.getRespuestaRuat() == null) result.setMotivo(motivo);
        return result.setReservaDto(ReservaMapper.toDto(reserva)).setResultado("ACTUALIZADA");
    }

    String datosAuditoria(Reserva reserva, Resultado result, String origen) {
        var data = mapper.createObjectNode();
        data.put("origenOperacion", origen);
        data.put("numeroReserva", reserva.getNumeroReserva());
        data.put("numeroTasa", reserva.getNumeroTasa());
        data.put("ruat", result.getRuat());
        data.set("estadoHttpRuat", mapper.valueToTree(result.getEstadoHttpRuat()));
        data.set("respuestaRuat", result.getRespuestaRuat());
        data.set("fechaLimitePago", mapper.valueToTree(reserva.getFechaLimitePago()));
        data.set("cancelacionAutomatica", mapper.valueToTree(reserva.getCancelacionAutomatica()));
        return data.toString();
    }

    JsonNode registrarRespuesta(Resultado result, ResponseEntity<byte[]> response) {
        result.setMotivo(null).setRespuestaRuat(null).setEstadoHttpRuat(null);
        if (response == null) return mapper.nullNode();
        result.setEstadoHttpRuat(response.getStatusCodeValue());
        byte[] bytes = response.getBody();
        if (bytes == null || bytes.length == 0) return mapper.nullNode();
        JsonNode body;
        try {
            body = mapper.readTree(bytes);
        } catch (java.io.IOException ex) {
            body = mapper.getNodeFactory().textNode(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        }
        if (body == null) body = mapper.nullNode();
        result.setRespuestaRuat(body);
        JsonNode message = body.path("mensaje");
        if (!message.isTextual()) message = body.path("datos").path("mensaje");
        if (!message.isTextual()) message = body.path("mensajeConfirmacion");
        if (!message.isTextual()) message = body.path("datos").path("mensajeConfirmacion");
        result.setMotivo(message.isTextual() ? message.textValue() : body.isTextual() ? body.textValue() : null);
        return body;
    }

    /** Called with the reservation locked, inside the cancellation transaction.
     * Never performs an annulment or interprets a failed query as an unpaid tax. */
    boolean verificarSinPagoParaAnular(Reserva reserva, Resultado result) {
        PagoRequest request = new PagoRequest();
        request.setNumeroTasa(reserva.getNumeroTasa());
        PagoToken token = new PagoToken();
        token.setPago(request);
        ResponseEntity<byte[]> response;
        try {
            response = ruat.consultaPago(token);
        } catch (RuntimeException ex) {
            sinRespuesta(result, ex);
            return false;
        }
        JsonNode body = registrarRespuesta(result, response);
        if (!consultaSinPago(response, body) && !confirmada(response, body)) {
            respuestaNoAceptada(result, response);
            return false;
        }
        JsonNode data = body.hasNonNull("datos") ? body.get("datos") : body;
        if (!data.isObject() || !data.has("pagoTasa")) {
            result.setResultado("ERROR").setRuat("RESPUESTA_INVALIDA")
                    .setDetalleLocal("La respuesta no contiene el campo pagoTasa; no se solicitó anulación.");
            return false;
        }
        if (data.hasNonNull("pagoTasa")) {
            LocalDateTime now = LocalDateTime.now(clock);
            Pago pago;
            try {
                pago = pagoConfirmado(reserva, data.get("pagoTasa"), body, now);
            } catch (RuntimeException ex) {
                result.setResultado("ERROR").setRuat("RESPUESTA_INVALIDA")
                        .setDetalleLocal("Los datos recibidos no permiten registrar el pago; no se solicitó anulación.");
                return false;
            }
            registrarPago(pago);
            result.setRuat("PAGO_CONFIRMADO");
            String estado = "periodo".equalsIgnoreCase(reserva.getTipoReserva()) ? "PAGADA"
                    : finalizada(reserva, now) ? "COMPLETADA" : enCurso(reserva, now) ? "EN_CURSO" : "PAGADA";
            cambiar(reserva, result, estado, "Pago confirmado durante la solicitud de anulación.", now,
                    "CONSULTA_PREVIA_ANULACION_MANUAL");
            result.setDetalleLocal("No se anuló la tasa: RUAT confirmó el pago.");
            return false;
        }
        result.setRuat("SIN_PAGO");
        if (reserva.getPagosList().stream().anyMatch(p ->
                "PAGADO".equalsIgnoreCase(p.getEstadoPago()) || "CONFIRMADO".equalsIgnoreCase(p.getEstadoPago()))) {
            result.setResultado("ERROR")
                    .setDetalleLocal("Existe un pago local confirmado; no se solicitó anulación.");
            return false;
        }
        return true;
    }

    boolean confirmada(ResponseEntity<byte[]> response, JsonNode body) {
        return response != null && response.getStatusCode().is2xxSuccessful() && body.isObject()
                && "true".equalsIgnoreCase(body.path("continuarFlujo").asText());
    }

    private boolean consultaSinPago(ResponseEntity<byte[]> response, JsonNode body) {
        if (response == null || (response.getStatusCodeValue() != 404 && response.getStatusCodeValue() != 200)) return false;
        String origin = response.getHeaders().getFirst("X-Response-Origin");
        String bodyOrigin = body.path("detalle").path("origen").asText();
        if ("ruat-be".equalsIgnoreCase(origin) || "deportes-be".equalsIgnoreCase(origin)
                || "ruat-be".equalsIgnoreCase(bodyOrigin) || "deportes-be".equalsIgnoreCase(bodyOrigin)) return false;
        JsonNode data = body.hasNonNull("datos") ? body.get("datos") : body;
        JsonNode message = body.hasNonNull("mensaje") ? body.get("mensaje") : data.path("mensaje");
        // Only known payment-query outcomes. Never reinterpret arbitrary 404s or annulment responses.
        return "false".equalsIgnoreCase(body.path("continuarFlujo").asText())
                && MENSAJES_SIN_PAGO.contains(message.asText().trim())
                && data.has("pagoTasa") && data.get("pagoTasa").isNull();
    }

    private Resultado respuestaNoAceptada(Resultado result, ResponseEntity<byte[]> response) {
        boolean unavailable = response == null || response.getStatusCode().is5xxServerError();
        if (result.getRespuestaRuat() == null) result.setDetalleLocal("No se recibió un cuerpo de respuesta.");
        return result.setResultado("ERROR").setRuat(unavailable ? "NO_DISPONIBLE" : "NO_CONFIRMADO");
    }

    private Resultado sinRespuesta(Resultado result, RuntimeException ex) {
        log.warn("Revisión RUAT sin respuesta HTTP completa: {}", ex.getClass().getSimpleName());
        return result.setResultado("ERROR").setRuat("NO_DISPONIBLE").setMotivo(null)
                .setRespuestaRuat(null).setEstadoHttpRuat(null)
                .setDetalleLocal("No se recibió una respuesta HTTP completa; no se aplicó el cambio local.");
    }

    private Pago pagoConfirmado(Reserva reserva, JsonNode payment, JsonNode body, LocalDateTime now) {
        if (!payment.isObject() || !reserva.getNumeroTasa().equals(payment.path("numeroTasa").asText())
                || !hasText(payment.path("folio").asText())) throw new IllegalArgumentException("Pago inválido");
        LocalDate fecha = LocalDate.parse(payment.path("fechaPago").asText(), FECHA_RUAT);
        BigDecimal monto = new BigDecimal(payment.path("montoPago").asText());
        if (monto.signum() < 0) throw new IllegalArgumentException("Monto inválido");
        return new Pago().setReserva(reserva).setNumeroTasa(reserva.getNumeroTasa()).setTipoTasa("TO")
                .setFolio(payment.path("folio").asText()).setFormaPago(payment.path("formaPago").asText(null))
                .setFechaPago(Date.from(fecha.atTime(12, 0).atZone(ZONE).toInstant()))
                .setEntidadFinanciera(payment.path("entidadFinanciera").asText(null)).setMontoPagado(monto)
                .setObservaciones(payment.path("observacion").asText(null)).setEstadoPago("PAGADO")
                .setUltimaConsulta(now).setRespuestaCompletaRuat(body.toString());
    }

    private void registrarPago(Pago pago) {
        Optional<Pago> existing = pagos.findByFolioAndReservaUuid(pago.getFolio(), pago.getReserva().getUuid());
        if (existing.isEmpty()) existing = pagos.findByFolioAndNumeroTasa(pago.getFolio(), pago.getNumeroTasa());
        if (existing.isPresent()) return;
        Pago saved = pagos.save(pago);
        auditoriaService.registrarPago("CREATE", null, saved);
        pago.getReserva().getPagosList().add(saved);
        pagoNotificaciones.notificarDespuesDeConfirmar(saved.getUuid());
    }

    private boolean finalizada(Reserva reserva, LocalDateTime now) {
        // School bases stay open for monthly registrations until an authorized user closes them.
        // Their date range still limits field usage and the available monthly periods.
        if (reserva.esRecurrenteGratuitaDeEscuela()) return false;
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())) {
            ReservaRecurrente rec = reserva.getReservaRecurrente();
            return rec != null && rec.getFechaFin() != null && now.toLocalDate().isAfter(rec.getFechaFin());
        }
        ReservaPuntual p = reserva.getReservaPuntual();
        return p != null && p.getFechaUso() != null && p.getHoraFin() != null
                && now.isAfter(p.getFechaUso().atTime(p.getHoraFin()));
    }

    private boolean enCurso(Reserva reserva, LocalDateTime now) {
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())) {
            ReservaRecurrente rec = reserva.getReservaRecurrente();
            if (rec == null || rec.getFechaInicio() == null || rec.getFechaFin() == null
                    || now.toLocalDate().isBefore(rec.getFechaInicio()) || now.toLocalDate().isAfter(rec.getFechaFin())
                    || rec.getDiasSemana() == null) return false;
            String[] days = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};
            String today = days[now.getDayOfWeek().getValue() - 1];
            return Arrays.stream(rec.getDiasSemana().split(",")).map(String::trim)
                    .map(s -> Normalizer.normalize(s.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                            .replaceAll("\\p{M}", ""))
                    .anyMatch(today::equals);
        }
        ReservaPuntual p = reserva.getReservaPuntual();
        return p != null && p.getFechaUso() != null && p.getHoraInicio() != null && p.getHoraFin() != null
                && !now.isBefore(p.getFechaUso().atTime(p.getHoraInicio()))
                && !now.isAfter(p.getFechaUso().atTime(p.getHoraFin()));
    }

    // Legacy calculation retained for reference only. Not used by monthly-fee status updates.
    @Deprecated
    private boolean mesFinalizado(Reserva reserva, LocalDate today) {
        // Original mensualidad screen used the first detail of the root reservation.
        if (reserva.getReservasPeriodoList() == null || reserva.getReservasPeriodoList().isEmpty()) return false;
        ReservaPeriodo p = reserva.getReservasPeriodoList().get(0);
        return p.getAño() != null && p.getMes() != null && p.getMes() >= 1 && p.getMes() <= 12
                && YearMonth.of(p.getAño(), p.getMes()).isBefore(YearMonth.from(today));
    }
    private String normal(String value) { return value == null ? "" : value.toUpperCase(Locale.ROOT); }
    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
