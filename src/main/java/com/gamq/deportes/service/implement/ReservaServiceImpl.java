package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.mapper.ReservaMapper;
import com.gamq.deportes.dto.mapper.ReservaPeriodoMapper;
import com.gamq.deportes.dto.mapper.ReservaPuntualMapper;
import com.gamq.deportes.dto.mapper.ReservaRecurrenteMapper;
import com.gamq.deportes.dto.mapper.ReservaTarifaDetalleMapper;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ReservaService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gamq.deportes.security.DeportesPrincipal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaServiceImpl.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private static final String NOTIFICAR_REGISTRO_RESERVA = "NOTIFICAR_REGISTRO_RESERVA";
    private static final String NOTIFICAR_RESULTADO_SOLICITUD = "NOTIFICAR_RESULTADO_SOLICITUD";
    private static final String NOTIFICAR_RESERVA_ANULADA = "NOTIFICAR_RESERVA_ANULADA";
    private static final String HORAS_VALIDACION_PAGO = "HORAS_VALIDACION_PAGO";
    private static final String HORAS_VALIDACION_PAGO_EVENTOS = "HORAS_VALIDACION_PAGO_EVENTOS";
    private static final String HORAS_VALIDACION_PAGO_RECURRENTES = "HORAS_VALIDACION_PAGO_RECURRENTES";
    private static final int HORAS_VALIDACION_PAGO_DEFAULT = 4;
    private static final String HABILITAR_RESERVAS_EVENTOS = "HABILITAR_RESERVAS_EVENTOS";
    private static final String HABILITAR_RESERVAS_ORGANIZACIONES = "HABILITAR_RESERVAS_ORGANIZACIONES";
    private static final String HABILITAR_RESERVAS_RECURRENTES = "HABILITAR_RESERVAS_RECURRENTES";
    private static final String HABILITAR_SOLICITUD_RESERVAS_EVENTOS = "HABILITAR_SOLICITUD_RESERVAS_EVENTOS";
    private static final String HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES = "HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES";
    private static final String HABILITAR_SOLICITUD_RESERVAS_RECURRENTES = "HABILITAR_SOLICITUD_RESERVAS_RECURRENTES";
    private static final String HORAS_ANTICIPACION_RESERVAS_PUNTUALES = "HORAS_ANTICIPACION_RESERVAS_PUNTUALES";
    private static final String HORAS_ANTICIPACION_RECURRENTES = "HORAS_ANTICIPACION_RECURRENTES";
    private static final String DIAS_ANTICIPACION_EVENTOS = "DIAS_ANTICIPACION_EVENTOS";
    private static final String DURACION_MINIMA_RESERVA = "DURACION_MINIMA_RESERVA";
    private static final String DURACION_MAXIMA_PUNTUALES = "DURACION_MAXIMA_PUNTUALES";
    private static final String DURACION_MAXIMA_EVENTOS = "DURACION_MAXIMA_EVENTOS";
    private static final String DURACION_MAXIMA_RECURRENTES = "DURACION_MAXIMA_RECURRENTES";

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private CampoDeportivoRepository campoDeportivoRepository;

    @Autowired
    private ContribuyenteRepository contribuyenteRepository;

    @Autowired
    private OrganizacionRepository organizacionRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Autowired
    private EstudianteEscuelaRepository estudianteEscuelaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaAuditoriaRepository reservaAuditoriaRepository;

    @Autowired
    private ReservaPuntualRepository reservaPuntualRepository;

    @Autowired
    private ReservaRecurrenteRepository reservaRecurrenteRepository;

    @Autowired
    private ReservaPeriodoRepository reservaPeriodoRepository;

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private ConfiguracionSistemaRepository configuracionSistemaRepository;

    @Autowired
    private ReservationNumberService reservationNumberService;

    @Autowired
    private ReservaPeriodoMapper reservaPeriodoMapper; // Add this

    @Autowired
    private ReservaNotificacionService reservaNotificacionService;

    @Autowired
    private NotificacionDestinatariosReservaService destinatariosService;

    @Override
    public Page<ReservaDto> obtenerReservas(
            Pageable pageable,
            Integer tab,
            String searchTerm,
            String typeReserva,
            String estadoReserva,
            String estadoValidacion,
            Boolean esReservaGratuita,
            String organization,
            Integer mesFiltro,
            Integer anioFiltro
    ) {

        // Quick filters from the tabs
        if (tab != null) {
            switch (tab) {
                case 1: // Pendientes de Pago
                    estadoReserva = "PENDIENTE";
                    break;

                case 2: // Pagadas
                    estadoReserva = "PAGADA";
                    break;

                case 3: // Vigentes
                    estadoReserva = "EN_CURSO";
                    break;

                case 4: // Completadas
                    estadoReserva = "COMPLETADA";
                    break;

                case 5: // Canceladas
                case 6: // Canceladas (legacy frontend tab id)
                    estadoReserva = "CANCELADA";
                    break;

                case 0: // Todo
                default:
                    break;
            }
        }

        return reservaRepository.buscarReservas(
                searchTerm,
                typeReserva,
                estadoReserva,
                estadoValidacion,
                esReservaGratuita,
                organization,
                mesFiltro,
                anioFiltro,
                pageable
        ).map(ReservaMapper::toDto);
    }
//    @Override
//    public List<ReservaDto> obtenerReservas() {
//        List<Reserva> reservas = reservaRepository.findAll();
//
//        if (reservas.isEmpty()) {
//            return List.of();
//        }
//
//        return reservas.stream()
//                .map(ReservaMapper::toDto)
//                .collect(Collectors.toList());
//    }

    public Page<ReservaDto> obtenerReservas(Pageable pageable) {
        return reservaRepository.findAll(pageable)
                .map(ReservaMapper::toDto);
    }


    @Override
    @Transactional(readOnly = true)
    public ReservaDto findByUuid(String uuid) {
        Optional<Reserva> reservaOptional = reservaRepository.findByUuid(uuid);
        if (reservaOptional.isPresent()) {
            return ReservaMapper.toDto(reservaOptional.get());
        } else {
            throw new NotFoundException(RESERVA, uuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaDto findByNumeroReserva(String numeroReserva) {
        Optional<Reserva> reservaOptional =  reservaRepository.findByNumeroReserva(numeroReserva);
        if (reservaOptional.isPresent()) {
            return ReservaMapper.toDto(reservaOptional.get());
        } else {
            throw new NotFoundException(RESERVA, numeroReserva);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> buscarPorFechaReserva(LocalDateTime fechaIni, LocalDateTime fechaFin) {
        List<Reserva> reservas = reservaRepository.findByFechaReservaBetween(fechaIni, fechaFin);

        if (reservas.isEmpty()) {
            return List.of();
        }

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReservaDto> buscarPorFechaReserva(LocalDateTime fechaIni, LocalDateTime fechaFin, Pageable pageable) {
        return reservaRepository.findByFechaReservaBetween(fechaIni, fechaFin, pageable)
                .map(ReservaMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> findReservasByCampoDeportivo(String uuidCampo) {
        try {
            Optional<CampoDeportivo> optional =  campoDeportivoRepository.findByUuid(uuidCampo);
            if (!optional.isPresent()) {
                return List.of();
            }

            List<Reserva> reservas = reservaRepository.findByCampoDeportivoUuid(uuidCampo);

            return reservas.stream()
                    .map(ReservaMapper::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> findReservasByContribuyente(String uuidContribuyente) {
        try {
            Optional<Contribuyente> optional = contribuyenteRepository.findByUuid(uuidContribuyente);
            if (!optional.isPresent()) {
                return List.of();
            }

            List<Reserva> reservas = reservaRepository.findByContribuyenteUuid(uuidContribuyente);

            return reservas.stream()
                    .map(ReservaMapper::toDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of();
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> findByEstadoReserva(String estadoReserva) {
        List<Reserva> reservas = reservaRepository.findByEstadoReserva(estadoReserva);

        if (reservas.isEmpty()) {
            return List.of();
        }

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> findByTipoReserva(String tipoReserva) {
        List<Reserva> reservas = reservaRepository.findByTipoReserva(tipoReserva);

        if (reservas.isEmpty()) {
            return List.of();
        }

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaDto> findReservasActivasPorCampoYFecha(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin) {
        List<Reserva> reservas = reservaRepository.findReservasActivasPorCampoYFecha(uuidCampo, fechaIni, fechaFin);

        if (reservas.isEmpty()) {
            return List.of();
        }

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservaDto> findReservasConflictivas(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin, String excludeReservaUuid) {
        List<Reserva> reservas = reservaRepository.findReservasConflictivas(uuidCampo, fechaIni, fechaFin, excludeReservaUuid);

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservaDto crearReserva(ReservaDto reservaDto) {
        if (reservaDto == null) {
            throw new NullReferenceException(RESERVA);
        }

        // Validate required fields
        if (reservaDto.getCampoDeportivoDto() == null || reservaDto.getCampoDeportivoDto().getUuid() == null) {
            throw new IllegalArgumentException("El campo deportivo es requerido");
        }

        if (reservaDto.getContribuyenteDto() == null || reservaDto.getContribuyenteDto().getUuid() == null) {
            throw new IllegalArgumentException("El contribuyente es requerido");
        }

        if (reservaDto.getTipoReserva() == null || reservaDto.getTipoReserva().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de reserva es requerido");
        }
        validarPoliticasConfiguradasParaContribuyente(reservaDto);

        boolean esPeriodo = "periodo".equalsIgnoreCase(reservaDto.getTipoReserva());
        if (esPeriodo) {
            validarCabeceraPeriodo(reservaDto);
        }

        CampoDeportivo campoDeportivo = getCampoDeportivo(reservaDto.getCampoDeportivoDto().getUuid());
        Contribuyente contribuyente = getContribuyente(reservaDto.getContribuyenteDto().getUuid());
        validateTaxpayerEligibleForReservation(contribuyente);

        // Handle tarifa - can be null for periodo reservations
        Tarifa tarifa = null;
        if (reservaDto.getTarifaDto() != null && reservaDto.getTarifaDto().getUuid() != null) {
            tarifa = getTarifa(reservaDto.getTarifaDto().getUuid());
        }

        // Handle reservation number - FIXED
        String numeroReserva = reservaDto.getNumeroReserva();
        boolean isUserProvided = false;

        if (numeroReserva != null && !numeroReserva.trim().isEmpty()) {
            // Validate user-provided number
            if (!reservationNumberService.isReservationNumberValid(numeroReserva, reservaDto.getTipoReserva())) {
                throw new IllegalArgumentException("Formato de número de reserva inválido: " + numeroReserva);
            }

            if (!reservationNumberService.isReservationNumberAvailable(numeroReserva)) {
                throw new IllegalArgumentException("El número de reserva ya está en uso: " + numeroReserva);
            }
            isUserProvided = true;
        } else {
            // Generate number based on the reservation type
            numeroReserva = reservationNumberService.findNextAvailableReservationNumber(reservaDto.getTipoReserva());
            isUserProvided = false;
        }

        Reserva nuevaReserva = ReservaMapper.toEntity(reservaDto);
        nuevaReserva.setCampoDeportivo(campoDeportivo);
        nuevaReserva.setContribuyente(contribuyente);
        nuevaReserva.setTarifa(tarifa); // This can be null
        nuevaReserva.setNumeroReserva(numeroReserva);
        nuevaReserva.setTipoReserva(reservaDto.getTipoReserva()); // Ensure tipo is set

        if (reservaDto.getOrganizacionDto() != null && reservaDto.getOrganizacionDto().getUuid() != null) {
            Organizacion organizacion = getOrganizacion(reservaDto.getOrganizacionDto().getUuid());
            nuevaReserva.setOrganizacion(organizacion);
        }

        nuevaReserva.setFechaCreacion(LocalDateTime.now());
        nuevaReserva.setFechaActualizacion(LocalDateTime.now());
        aplicarPoliticaPago(nuevaReserva);

        // Don't generate again if we already have a number
        // Removed: if (nuevaReserva.getNumeroReserva() == null) {
        //    nuevaReserva.setNumeroReserva(generarNumeroReserva());
        // }

        if ("puntual".equalsIgnoreCase(reservaDto.getTipoReserva()) && reservaDto.getReservaPuntualDto() != null) {
            ReservaPuntual puntual = ReservaPuntualMapper.toEntity(reservaDto.getReservaPuntualDto(), false);
            puntual.setReserva(nuevaReserva);
            nuevaReserva.setReservaPuntual(puntual);
        }

        if ("recurrente".equalsIgnoreCase(reservaDto.getTipoReserva()) && reservaDto.getReservaRecurrenteDto() != null) {
            ReservaRecurrente recurrente = ReservaRecurrenteMapper.toEntity(reservaDto.getReservaRecurrenteDto(), false);

            if (recurrente.getTablero() != null && recurrente.getTablero().getIdTablero() == null) {
                Tablero tableroGuardado = tableroRepository.save(recurrente.getTablero());
                recurrente.setTablero(tableroGuardado);
            }

            recurrente.setReserva(nuevaReserva);
            nuevaReserva.setReservaRecurrente(recurrente);
        }

        if (esPeriodo) {
            agregarDetallesPeriodo(nuevaReserva, reservaDto.getReservasPeriodoDtoList());
        }

        if (reservaDto.getTarifaDetallesDtoList() != null && !reservaDto.getTarifaDetallesDtoList().isEmpty()) {
            reservaDto.getTarifaDetallesDtoList().forEach(detalleDto -> {
                ReservaTarifaDetalle detalle = ReservaTarifaDetalleMapper.toEntity(detalleDto);
                detalle.setReserva(nuevaReserva);

                if (detalleDto.getTarifaDto() != null && detalleDto.getTarifaDto().getUuid() != null) {
                    detalle.setTarifa(getTarifa(detalleDto.getTarifaDto().getUuid()));
                }

                if (detalleDto.getTarifaConceptoDto() != null && detalleDto.getTarifaConceptoDto().getUuid() != null) {
                    detalle.setTarifaConcepto(getTarifaConcepto(detalleDto.getTarifaConceptoDto().getUuid()));
                }

                nuevaReserva.getTarifaDetallesList().add(detalle);
            });
        }

        Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
        registrarAuditoria(
                reservaGuardada,
                "CREATE",
                null,
                reservaGuardada.getEstadoReserva(),
                reservaGuardada.getCreadoPor(),
                "Reserva creada",
                buildReservaSnapshot(reservaGuardada)
        );
        boolean esSolicitud = "SOLICITADA".equalsIgnoreCase(reservaGuardada.getEstadoReserva())
                || "PENDIENTE".equalsIgnoreCase(reservaGuardada.getEstadoValidacion());
        notificarReserva(reservaGuardada, NOTIFICAR_REGISTRO_RESERVA,
                esSolicitud ? "Solicitud registrada" : "Reserva creada",
                esSolicitud
                        ? "La solicitud fue registrada y está pendiente de revisión"
                        : "La reserva fue creada correctamente",
                "SUCCESS");
        return ReservaMapper.toDto(reservaGuardada);
    }

    private void validateTaxpayerEligibleForReservation(Contribuyente contribuyente) {
        if (Boolean.TRUE.equals(contribuyente.getEstaSancionado())
                || "SANCIONADO".equalsIgnoreCase(contribuyente.getEstadoUsuario())) {
            throw new IllegalStateException(
                    "El contribuyente no puede realizar nuevas reservas mientras su cuenta esté sancionada");
        }
        if (!contribuyente.estaActivo()) {
            throw new IllegalStateException(
                    "El contribuyente debe tener una cuenta activa para realizar nuevas reservas");
        }
    }
    @Override
    public ReservaDto actualizarReserva(ReservaDto reservaDto) {
        if (reservaDto == null) {
            throw new NullReferenceException(RESERVA);
        }

        Optional<Reserva> reservaOptional = reservaRepository.findByUuid(reservaDto.getUuid());

        if (reservaOptional.isPresent()) {
            Reserva reservaExistente = reservaOptional.get();
            String estadoAnterior = reservaExistente.getEstadoReserva();
            String estadoValidacionAnterior = reservaExistente.getEstadoValidacion();
            ReservaMapper.updateEntityFromDto(reservaDto, reservaExistente);

            if (!Objects.equals(estadoValidacionAnterior, reservaExistente.getEstadoValidacion())
                    && "RECHAZADA".equalsIgnoreCase(reservaExistente.getEstadoValidacion())) {
                validarMotivo(reservaExistente.getMotivoRechazo(), "El motivo de rechazo es requerido");
            }
            if (!Objects.equals(estadoAnterior, reservaExistente.getEstadoReserva())
                    && "CANCELADA".equalsIgnoreCase(reservaExistente.getEstadoReserva())) {
                validarMotivo(reservaExistente.getMotivoRechazo(), "El motivo de cancelación es requerido");
            }

            if (reservaDto.getCampoDeportivoDto() != null) {
                CampoDeportivo campoDeportivo = getCampoDeportivo(reservaDto.getCampoDeportivoDto().getUuid());
                reservaExistente.setCampoDeportivo(campoDeportivo);
            }

            if (reservaDto.getContribuyenteDto() != null) {
                Contribuyente contribuyente = getContribuyente(reservaDto.getContribuyenteDto().getUuid());
                reservaExistente.setContribuyente(contribuyente);
            }

            if (reservaDto.getTarifaDto() != null) {
                Tarifa tarifa = getTarifa(reservaDto.getTarifaDto().getUuid());
                reservaExistente.setTarifa(tarifa);
            }

            if (reservaDto.getOrganizacionDto() != null) {
                Organizacion organizacion = getOrganizacion(reservaDto.getOrganizacionDto().getUuid());
                reservaExistente.setOrganizacion(organizacion);
            }

            if ("periodo".equalsIgnoreCase(reservaExistente.getTipoReserva())
                    && reservaDto.getReservasPeriodoDtoList() != null
                    && !reservaDto.getReservasPeriodoDtoList().isEmpty()) {
                reservaExistente.getReservasPeriodoList().clear();
                agregarDetallesPeriodo(reservaExistente, reservaDto.getReservasPeriodoDtoList());
            }

            reservaExistente.setFechaActualizacion(LocalDateTime.now());
            aplicarPoliticaPago(reservaExistente);

            Reserva reservaActualizada = reservaRepository.save(reservaExistente);
            registrarAuditoria(
                    reservaActualizada,
                    "UPDATE",
                    estadoAnterior,
                    reservaActualizada.getEstadoReserva(),
                    reservaDto.getAprobadoPor(),
                    "Reserva actualizada",
                    "{\"estadoValidacionAnterior\":\"" + safeValue(estadoValidacionAnterior)
                            + "\",\"estadoValidacionNuevo\":\"" + safeValue(reservaActualizada.getEstadoValidacion()) + "\"}"
            );
            return ReservaMapper.toDto(reservaActualizada);
        } else {
            throw new NotFoundException(RESERVA, reservaDto.getUuid());
        }
    }

    @Override
    @Transactional
    public ReservaDto aprobarReserva(String uuid, String aprobadoPor) {
        Reserva reserva = reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        String estadoAnterior = reserva.getEstadoReserva();
        String estadoValidacionAnterior = reserva.getEstadoValidacion();

        reserva.setEstadoReserva("PENDIENTE");
        reserva.setEstadoValidacion("APROBADA");
        reserva.setAprobadoPor(aprobadoPor);
        reserva.setFechaAprobacion(LocalDateTime.now());
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaAprobada = reservaRepository.save(reserva);
        registrarAuditoria(
                reservaAprobada,
                "APPROVE",
                estadoAnterior,
                reservaAprobada.getEstadoReserva(),
                aprobadoPor,
                "Reserva aprobada",
                "{\"estadoValidacionAnterior\":\"" + safeValue(estadoValidacionAnterior)
                        + "\",\"estadoValidacionNuevo\":\"" + safeValue(reservaAprobada.getEstadoValidacion()) + "\"}"
        );
        notificarReserva(reservaAprobada, NOTIFICAR_RESULTADO_SOLICITUD, "Solicitud aprobada",
                "La solicitud fue aprobada",
                "SUCCESS");
        return ReservaMapper.toDto(reservaAprobada);
    }

    @Override
    @Transactional
    public ReservaDto rechazarReserva(String uuid, String motivoRechazo, String aprobadoPor) {
        validarMotivo(motivoRechazo, "El motivo de rechazo es requerido");
        Reserva reserva = reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        String estadoAnterior = reserva.getEstadoReserva();
        String estadoValidacionAnterior = reserva.getEstadoValidacion();

        reserva.setEstadoValidacion("RECHAZADA");
        reserva.setMotivoRechazo(motivoRechazo);
        reserva.setAprobadoPor(aprobadoPor);
        reserva.setFechaRechazo(LocalDateTime.now());
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaRechazada = reservaRepository.save(reserva);
        registrarAuditoria(
                reservaRechazada,
                "REJECT",
                estadoAnterior,
                reservaRechazada.getEstadoReserva(),
                aprobadoPor,
                "Reserva rechazada: " + motivoRechazo,
                "{\"estadoValidacionAnterior\":\"" + safeValue(estadoValidacionAnterior)
                        + "\",\"estadoValidacionNuevo\":\"" + safeValue(reservaRechazada.getEstadoValidacion()) + "\"}"
        );
        notificarReserva(reservaRechazada, NOTIFICAR_RESULTADO_SOLICITUD, "Solicitud rechazada",
                "La solicitud fue rechazada. Motivo: " + safeValue(motivoRechazo),
                "WARNING");
        return ReservaMapper.toDto(reservaRechazada);
    }

    @Override
    @Transactional
    public ReservaDto cancelarReserva(String uuid, String motivo, String anuladoPor) {
        validarMotivo(motivo, "El motivo de cancelación es requerido");
        Reserva reserva = reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        String estadoAnterior = reserva.getEstadoReserva();

        reserva.setEstadoReserva("CANCELADA");
        reserva.setMotivoRechazo(motivo);
        reserva.setAnuladoPor(anuladoPor);
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaCancelada = reservaRepository.save(reserva);
        registrarAuditoria(
                reservaCancelada,
                "CANCEL",
                estadoAnterior,
                reservaCancelada.getEstadoReserva(),
                anuladoPor,
                "Reserva cancelada: " + motivo,
                null
        );
        reservaNotificacionService.notificarCancelada(reservaCancelada);
        return ReservaMapper.toDto(reservaCancelada);
    }

    @Override
    @Transactional
    public String eliminarReserva(String uuid) {
        try {
            Reserva reserva = reservaRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(RESERVA, uuid));

            registrarAuditoria(
                    reserva,
                    "DELETE",
                    reserva.getEstadoReserva(),
                    reserva.getEstadoReserva(),
                    null,
                    "Reserva desactivada",
                    buildReservaSnapshot(reserva)
            );
            reservaRepository.delete(reserva);

            return "Desactivación exitosa del RESERVA";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el RESERVA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean existeReservaConflictiva(String uuidCampo, LocalDateTime fechaIni, LocalDateTime fechaFin, String excludeReservaUuid) {
        List<Reserva> reservasConflictivas = reservaRepository.findReservasConflictivas(uuidCampo, fechaIni, fechaFin, excludeReservaUuid);
        return !reservasConflictivas.isEmpty();
    }

    @Transactional(readOnly = true)
    public Long contarPorEstadoReserva(String estadoReserva) {
        return reservaRepository.countByEstadoReserva(estadoReserva);
    }

    @Transactional(readOnly = true)
    public Long contarPorCampoDeportivoYEstado(String uuidCampo, String estadoReserva) {
        return reservaRepository.countByCampoDeportivoAndEstadoReserva(uuidCampo, estadoReserva);
    }

    private CampoDeportivo getCampoDeportivo(String uuid) {
        return campoDeportivoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }

    private Contribuyente getContribuyente(String uuid) {
        return contribuyenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, uuid));
    }

    private Organizacion getOrganizacion(String uuid) {
        return organizacionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ORGANIZACION, uuid));
    }

    private Tarifa getTarifa(String uuid) {
        return tarifaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA, uuid));
    }

    private TarifaConcepto getTarifaConcepto(String uuid) {
        return tarifaConceptoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA_CONCEPTO, uuid));
    }

    private EstudianteEscuela getEstudianteEscuela(String uuid) {
        return estudianteEscuelaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ESTUDIANTE_ESCUELA, uuid));
    }

    private void validarCabeceraPeriodo(ReservaDto dto) {
        if (dto.getOrganizacionDto() == null || dto.getOrganizacionDto().getUuid() == null) {
            throw new IllegalArgumentException("La escuela es requerida para una reserva tipo periodo");
        }
        if (dto.getTarifaDto() == null || dto.getTarifaDto().getUuid() == null) {
            throw new IllegalArgumentException("La tarifa es requerida para una reserva tipo periodo");
        }
        if (dto.getMontoTotal() == null || dto.getMontoTotal().signum() < 0) {
            throw new IllegalArgumentException("El monto total de la tasa es requerido");
        }
        if (dto.getReservasPeriodoDtoList() == null || dto.getReservasPeriodoDtoList().isEmpty()) {
            throw new IllegalArgumentException("La reserva tipo periodo requiere al menos un detalle");
        }
    }

    private void agregarDetallesPeriodo(Reserva reserva, List<com.gamq.deportes.dto.ReservaPeriodoDto> detallesDto) {
        for (com.gamq.deportes.dto.ReservaPeriodoDto detalleDto : detallesDto) {
            if (detalleDto.getMes() == null || detalleDto.getMes() < 1 || detalleDto.getMes() > 12
                    || detalleDto.getAño() == null) {
                throw new IllegalArgumentException("Cada detalle debe tener un mes (1-12) y año válidos");
            }
            if (detalleDto.getEstudianteEscuelaDto() == null
                    || detalleDto.getEstudianteEscuelaDto().getUuid() == null) {
                throw new IllegalArgumentException("Cada detalle de periodo debe identificar un estudiante");
            }
            ReservaPeriodo periodo = ReservaPeriodoMapper.toEntity(detalleDto);
            periodo.setReserva(reserva);
            EstudianteEscuela inscripcion =
                    getEstudianteEscuela(detalleDto.getEstudianteEscuelaDto().getUuid());
            if (!inscripcion.estaActivo()) {
                throw new IllegalArgumentException("La inscripción del estudiante no está activa");
            }
            if (reserva.getOrganizacion() == null
                    || !inscripcion.getOrganizacionDto().getUuid()
                    .equals(reserva.getOrganizacion().getUuid())) {
                throw new IllegalArgumentException(
                        "La inscripción del estudiante no pertenece a la escuela de la mensualidad");
            }
            periodo.setEstudianteEscuela(inscripcion);
            periodo.setMatricula(inscripcion.getMatricula());
            reserva.getReservasPeriodoList().add(periodo);
        }
    }

    private void validarPoliticasConfiguradasParaContribuyente(ReservaDto dto) {
        if (!isContributorRequest()) {
            return;
        }

        boolean solicitud = "PENDIENTE".equalsIgnoreCase(dto.getEstadoValidacion())
                || "SOLICITADA".equalsIgnoreCase(dto.getEstadoReserva());
        boolean recurrente = "recurrente".equalsIgnoreCase(dto.getTipoReserva());
        boolean organizacion = dto.getOrganizacionDto() != null
                && dto.getOrganizacionDto().getUuid() != null;
        boolean evento = dto.getTarifaDto() != null
                && "por_evento".equalsIgnoreCase(dto.getTarifaDto().getTipoCobro());

        if (recurrente) {
            requireEnabled(solicitud
                    ? HABILITAR_SOLICITUD_RESERVAS_RECURRENTES
                    : HABILITAR_RESERVAS_RECURRENTES);
        }
        if (evento) {
            requireEnabled(solicitud
                    ? HABILITAR_SOLICITUD_RESERVAS_EVENTOS
                    : HABILITAR_RESERVAS_EVENTOS);
        }
        if (organizacion) {
            requireEnabled(solicitud
                    ? HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES
                    : HABILITAR_RESERVAS_ORGANIZACIONES);
        }

        if (recurrente && dto.getReservaRecurrenteDto() != null) {
            validarReservaRecurrente(dto);
        } else if (dto.getReservaPuntualDto() != null) {
            validarReservaPuntual(dto, evento);
        }
    }

    private void validarReservaPuntual(ReservaDto dto, boolean evento) {
        LocalDate fecha = dto.getReservaPuntualDto().getFechaUso();
        LocalTime inicio = dto.getReservaPuntualDto().getHoraInicio();
        LocalTime fin = dto.getReservaPuntualDto().getHoraFin();
        if (fecha == null || inicio == null || fin == null) {
            return;
        }

        LocalDateTime comienzo = LocalDateTime.of(fecha, inicio);
        double anticipacion = numericPolicy(
                evento ? DIAS_ANTICIPACION_EVENTOS : HORAS_ANTICIPACION_RESERVAS_PUNTUALES,
                0D);
        LocalDateTime minimo = evento
                ? LocalDateTime.now().plusMinutes(Math.round(anticipacion * 24D * 60D))
                : LocalDateTime.now().plusMinutes(Math.round(anticipacion * 60D));
        if (comienzo.isBefore(minimo)) {
            throw new IllegalArgumentException("La reserva no cumple la anticipación mínima configurada");
        }

        double duracion = Duration.between(inicio, fin).toMinutes() / 60D;
        double minima = numericPolicy(DURACION_MINIMA_RESERVA, 1D);
        double maxima = numericPolicy(
                evento ? DURACION_MAXIMA_EVENTOS : DURACION_MAXIMA_PUNTUALES,
                evento ? 8D : 8D);
        if (duracion < minima || duracion > maxima) {
            throw new IllegalArgumentException(
                    "La duración de la reserva debe estar entre " + minima + " y " + maxima + " horas");
        }
    }

    private void validarReservaRecurrente(ReservaDto dto) {
        LocalDate inicio = dto.getReservaRecurrenteDto().getFechaInicio();
        LocalDate fin = dto.getReservaRecurrenteDto().getFechaFin();
        if (inicio == null || fin == null) {
            return;
        }
        // La anticipación recurrente se valida en el selector de horarios, que sí
        // conoce la fecha y hora de la primera sesión. Este DTO solo trae fechas;
        // compararlo contra medianoche rechazaría reservas válidas.
        double maximoDias = numericPolicy(DURACION_MAXIMA_RECURRENTES, 90D);
        long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        if (dias < 1 || dias > maximoDias) {
            throw new IllegalArgumentException(
                    "La reserva recurrente no puede abarcar más de " + maximoDias + " días");
        }
        Integer duracionSesion = dto.getReservaRecurrenteDto().getDuracionSesionHoras();
        double minima = numericPolicy(DURACION_MINIMA_RESERVA, 1D);
        if (duracionSesion != null && duracionSesion < minima) {
            throw new IllegalArgumentException(
                    "Cada sesión debe durar al menos " + minima + " horas");
        }
    }

    private void requireEnabled(String key) {
        boolean enabled = configuracionSistemaRepository
                .findEffectiveCandidates(key, "contribuyente", null)
                .stream()
                .findFirst()
                .map(ConfiguracionSistema::getValorBoolean)
                .orElse(false);
        if (!enabled) {
            throw new IllegalArgumentException("La funcionalidad " + key + " está deshabilitada");
        }
    }

    private double numericPolicy(String key, double fallback) {
        return configuracionSistemaRepository
                .findEffectiveCandidates(key, "contribuyente", null)
                .stream()
                .findFirst()
                .map(ConfiguracionSistema::getValorNumerico)
                .filter(Objects::nonNull)
                .orElse(fallback);
    }

    private boolean isContributorRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.getPrincipal() instanceof DeportesPrincipal
                && "TAXPAYER".equalsIgnoreCase(
                        ((DeportesPrincipal) authentication.getPrincipal()).getUserType());
    }

    private void aplicarPoliticaPago(Reserva reserva) {
        if (reserva.getCancelacionAutomatica() == null) {
            reserva.setCancelacionAutomatica(true);
        }

        if (Boolean.TRUE.equals(reserva.getCancelacionAutomatica())) {
            reserva.setMotivoCancelacionAutomatica(null);
            reserva.setAutorizadoPor(null);
            if (reserva.getFechaLimitePago() == null) {
                reserva.setFechaLimitePago(LocalDateTime.now().plusHours(obtenerHorasValidacionPago(reserva)));
            }
            return;
        }

        reserva.setFechaLimitePago(null);
        if (reserva.getMotivoCancelacionAutomatica() == null || reserva.getMotivoCancelacionAutomatica().trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo para no anular automaticamente la reserva por falta de pago es requerido");
        }
    }

    private int obtenerHorasValidacionPago(Reserva reserva) {
        String clave = obtenerClaveValidacionPago(reserva);
        return obtenerValorNumericoActivo(clave)
                .or(() -> obtenerValorNumericoActivo(HORAS_VALIDACION_PAGO))
                .orElse(HORAS_VALIDACION_PAGO_DEFAULT);
    }

    private String obtenerClaveValidacionPago(Reserva reserva) {
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())
                || "periodo".equalsIgnoreCase(reserva.getTipoReserva())) {
            return HORAS_VALIDACION_PAGO_RECURRENTES;
        }
        if (reserva.getTarifa() != null && "por_evento".equalsIgnoreCase(reserva.getTarifa().getTipoCobro())) {
            return HORAS_VALIDACION_PAGO_EVENTOS;
        }
        return HORAS_VALIDACION_PAGO;
    }

    private java.util.Optional<Integer> obtenerValorNumericoActivo(String clave) {
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .filter(config -> Boolean.TRUE.equals(config.getActivo()))
                .map(ConfiguracionSistema::getValorNumerico)
                .filter(value -> value != null && value > 0)
                .map(Double::intValue);
    }

    private void registrarAuditoria(Reserva reserva, String accion, String estadoAnterior, String estadoNuevo,
                                    String usuarioUuid, String observaciones, String datosCambio) {
        ReservaAuditoria auditoria = new ReservaAuditoria();
        auditoria.setReserva(reserva);
        auditoria.setAccion(accion);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setUsuarioUuid(usuarioUuid);
        auditoria.setObservaciones(observaciones);
        auditoria.setDatosCambio(normalizeJsonText(datosCambio));
        auditoria.setFechaCreacion(LocalDateTime.now());
        auditoria.setFechaAccion(LocalDateTime.now());
        try {
            reservaAuditoriaRepository.save(auditoria);
        } catch (RuntimeException ex) {
            logger.error("No se pudo registrar auditoria de reserva {} accion {}", reserva.getUuid(), accion, ex);
        }
    }

    private String normalizeJsonText(String datosCambio) {
        if (datosCambio == null || datosCambio.trim().isEmpty()) {
            return null;
        }
        return datosCambio;
    }

    private String buildReservaSnapshot(Reserva reserva) {
        return "{\"uuid\":\"" + safeValue(reserva.getUuid())
                + "\",\"numeroReserva\":\"" + safeValue(reserva.getNumeroReserva())
                + "\",\"tipoReserva\":\"" + safeValue(reserva.getTipoReserva())
                + "\",\"estadoReserva\":\"" + safeValue(reserva.getEstadoReserva())
                + "\",\"estadoValidacion\":\"" + safeValue(reserva.getEstadoValidacion())
                + "\",\"fechaLimitePago\":\"" + safeValue(reserva.getFechaLimitePago())
                + "\",\"cancelacionAutomatica\":\"" + safeValue(reserva.getCancelacionAutomatica())
                + "\",\"montoTotal\":\"" + safeValue(reserva.getMontoTotal()) + "\"}";
    }

    private void notificarReserva(Reserva reserva, String tipo, String titulo, String mensaje, String prioridad) {
        if (reserva.getContribuyente() == null || reserva.getContribuyente().getUuid() == null) {
            return;
        }
        NotificacionDto comun = new NotificacionDto()
                .setTipo(tipo)
                .setTitulo(ReservaNotificacionFormatter.titulo(titulo, reserva))
                .setMensaje(ReservaNotificacionFormatter.mensaje(reserva, mensaje))
                .setPrioridad(prioridad)
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(reserva.getUuid())
                .setAccionUrl(ReservaNotificacionFormatter.ruta(reserva));
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

    private String detalleNotificacion(Reserva reserva) {
        StringBuilder detalle = new StringBuilder();
        if (reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank()) {
            detalle.append(" Número de tasa: ").append(safeValue(reserva.getNumeroTasa())).append(".");
        }
        if (reserva.getCampoDeportivo() != null) {
            detalle.append(" Lugar: ").append(safeValue(reserva.getCampoDeportivo().getNombre()));
            if (reserva.getCampoDeportivo().getComplejo() != null) {
                detalle.append(", complejo ")
                        .append(safeValue(reserva.getCampoDeportivo().getComplejo().getNombre()));
            }
            detalle.append(".");
        }
        if (reserva.getReservaPuntual() != null) {
            ReservaPuntual puntual = reserva.getReservaPuntual();
            if (puntual.getFechaUso() != null) {
                detalle.append(" Fecha: ").append(puntual.getFechaUso().format(FORMATO_FECHA)).append(".");
            }
            if (puntual.getHoraInicio() != null) {
                detalle.append(" Horario: ").append(puntual.getHoraInicio().format(FORMATO_HORA));
                if (puntual.getHoraFin() != null) {
                    detalle.append(" a ").append(puntual.getHoraFin().format(FORMATO_HORA));
                }
                detalle.append(".");
            }
        } else if (reserva.getReservaRecurrente() != null) {
            ReservaRecurrente recurrente = reserva.getReservaRecurrente();
            if (recurrente.getFechaInicio() != null) {
                detalle.append(" Periodo: ")
                        .append(recurrente.getFechaInicio().format(FORMATO_FECHA));
                if (recurrente.getFechaFin() != null) {
                    detalle.append(" al ").append(recurrente.getFechaFin().format(FORMATO_FECHA));
                }
                detalle.append(".");
            }
            if (recurrente.getDiasSemana() != null && !recurrente.getDiasSemana().isBlank()) {
                detalle.append(" Días: ").append(safeValue(recurrente.getDiasSemana())).append(".");
            }
        } else if (reserva.getReservasPeriodoList() != null
                && !reserva.getReservasPeriodoList().isEmpty()) {
            String periodos = reserva.getReservasPeriodoList().stream()
                    .filter(periodo -> periodo.getMes() != null && periodo.getAño() != null)
                    .map(periodo -> periodo.getMes() + "/" + periodo.getAño())
                    .distinct()
                    .collect(Collectors.joining(", "));
            if (!periodos.isEmpty()) {
                detalle.append(" Periodo: ").append(periodos).append(".");
            }
        }
        return detalle.toString();
    }

    private void validarMotivo(String motivo, String mensaje) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }
}
