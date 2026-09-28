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
import org.springframework.data.jpa.domain.Specification;
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
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.criteria.JoinType;

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
    private ReservaRuatRevisionService ruatRevisionService;

    @Autowired
    private com.gamq.deportes.service.ApiService apiService;

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
    private ReservaPeriodoMapper reservaPeriodoMapper;

    @Autowired
    private ReservaNotificacionService reservaNotificacionService;

    @Autowired
    private com.gamq.deportes.security.ReservationListScope listScope;

    @Autowired
    private NotificacionDestinatariosReservaService destinatariosService;

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
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
            Integer anioFiltro,
            boolean revisarEstados
    ) {

        Specification<Reserva> spec = listScope.reservations();
        final Specification<Reserva> reviewScope = typeReserva == null || typeReserva.isBlank() ? spec
                : spec.and((root, query, cb) -> cb.equal(root.get("tipoReserva"), typeReserva));

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

        final String resolvedTypeReserva = typeReserva;
        final String resolvedEstadoReserva = estadoReserva;
        final String resolvedEstadoValidacion = estadoValidacion;
        final Boolean resolvedEsReservaGratuita = esReservaGratuita;
        final String resolvedOrganization = organization;
        final Integer resolvedMesFiltro = mesFiltro;
        final Integer resolvedAnioFiltro = anioFiltro;

        if (resolvedTypeReserva != null && !resolvedTypeReserva.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("tipoReserva"), resolvedTypeReserva));
        }
        if (resolvedEstadoReserva != null && !resolvedEstadoReserva.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoReserva"), resolvedEstadoReserva));
        }
        if (resolvedEstadoValidacion != null && !resolvedEstadoValidacion.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("estadoValidacion"), resolvedEstadoValidacion));
        }
        if (resolvedEsReservaGratuita != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("esReservaGratuita"), resolvedEsReservaGratuita));
        }
        if (resolvedOrganization != null && !resolvedOrganization.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(
                    root.join("organizacion", JoinType.LEFT).get("uuid"), resolvedOrganization));
        }
        if (resolvedMesFiltro != null || resolvedAnioFiltro != null) {
            spec = spec.and((root, query, cb) -> {
                javax.persistence.criteria.Subquery<Long> matchingPeriod = query.subquery(Long.class);
                javax.persistence.criteria.Root<ReservaPeriodo> period = matchingPeriod.from(ReservaPeriodo.class);
                javax.persistence.criteria.Predicate result = cb.and(cb.equal(period.get("reserva"), root),
                        cb.isFalse(period.get("estado")));
                if (resolvedMesFiltro != null) result = cb.and(result, cb.equal(period.get("mes"), resolvedMesFiltro));
                if (resolvedAnioFiltro != null) result = cb.and(result, cb.equal(period.get("año"), resolvedAnioFiltro));
                matchingPeriod.select(period.get("idReservaPeriodo")).where(result);
                return cb.exists(matchingPeriod);
            });
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            String term = "%" + searchTerm.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                javax.persistence.criteria.Join<Reserva, Contribuyente> contributor =
                        root.join("contribuyente", JoinType.LEFT);
                javax.persistence.criteria.Join<Reserva, Organizacion> organizationJoin =
                        root.join("organizacion", JoinType.LEFT);
                javax.persistence.criteria.Join<Reserva, CampoDeportivo> field =
                        root.join("campoDeportivo", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(cb.concat(cb.concat(cb.coalesce(contributor.get("nombre"), ""), " "),
                                cb.concat(cb.coalesce(contributor.get("primerApellido"), ""),
                                        cb.concat(" ", cb.coalesce(contributor.get("segundoApellido"), ""))))), term),
                        cb.like(cb.lower(organizationJoin.get("nombre")), term),
                        cb.like(cb.lower(field.get("nombre")), term),
                        cb.like(cb.lower(root.get("numeroReserva")), term),
                        cb.like(cb.lower(root.get("numeroTasa")), term),
                        cb.like(cb.lower(root.get("motivoRechazo")), term),
                        cb.like(cb.lower(root.get("motivoGratuidad")), term)
                );
            });
        }

        final Specification<Reserva> listSpec = spec;
        if (!revisarEstados) return ruatRevisionService.leerPagina(
                () -> reservaRepository.findAll(listSpec, pageable), ReservaMapper::toDto);
        return ruatRevisionService.revisarLista(() -> reservaRepository.findAll(listSpec, pageable),
                r -> r, ReservaMapper::toDto,
                after -> reservaRepository.findAll(
                        ReservaRuatRevisionService.candidatas(reviewScope, "", after),
                        org.springframework.data.domain.PageRequest.of(0, 100,
                                org.springframework.data.domain.Sort.by("idReserva"))));
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
        validarContribuyentePropio(contribuyente);
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
            validarContribuyentePuedeGenerarMensualidad(nuevaReserva);
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

            // Expiration must only be committed by the RUAT-first review, not by a stale client PUT.
            if ("EXPIRADA".equalsIgnoreCase(reservaDto.getEstadoReserva())
                    && !"EXPIRADA".equalsIgnoreCase(estadoAnterior)) {
                throw new IllegalArgumentException(
                        "La expiración requiere consultar el pago y confirmar la anulación en la revisión RUAT");
            }

            if (reservaExistente.getNumeroTasa() != null && !reservaExistente.getNumeroTasa().isBlank()) {
                throw new IllegalStateException("Una reserva con tasa no admite edición general. Utilice la acción específica autorizada.");
            }
            if (esActualizacionEstadoSimple(reservaDto)) {
                reservaExistente.setEstadoReserva(reservaDto.getEstadoReserva());
                reservaExistente.setFechaActualizacion(
                        reservaDto.getFechaActualizacion() != null
                                ? reservaDto.getFechaActualizacion()
                                : LocalDateTime.now());
                Reserva reservaActualizada = reservaRepository.save(reservaExistente);
                registrarAuditoria(
                        reservaActualizada,
                        "UPDATE",
                        estadoAnterior,
                        reservaActualizada.getEstadoReserva(),
                        reservaDto.getAprobadoPor(),
                        "Estado de reserva actualizado",
                        null
                );
                return ReservaMapper.toDto(reservaActualizada);
            }

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
                validarContribuyentePropio(contribuyente);
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
                actualizarDetallesPeriodo(reservaExistente, reservaDto.getReservasPeriodoDtoList());
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

    private boolean esActualizacionEstadoSimple(ReservaDto reservaDto) {
        return reservaDto.getEstadoReserva() != null
                && reservaDto.getFechaReserva() == null
                && reservaDto.getFechaLimitePago() == null
                && reservaDto.getAutorizadoPor() == null
                && reservaDto.getFechaAprobacion() == null
                && reservaDto.getFechaRechazo() == null
                && reservaDto.getMotivoRechazo() == null
                && reservaDto.getAprobadoPor() == null
                && reservaDto.getCreadoPor() == null
                && reservaDto.getAnuladoPor() == null
                && reservaDto.getEstadoValidacion() == null
                && reservaDto.getTipoReserva() == null
                && reservaDto.getNumeroReserva() == null
                && reservaDto.getNumeroTasa() == null
                && reservaDto.getMontoTotal() == null
                && reservaDto.getClasificadorRuatId() == null
                && reservaDto.getCampoDeportivoDto() == null
                && reservaDto.getContribuyenteDto() == null
                && reservaDto.getOrganizacionDto() == null
                && reservaDto.getTarifaDto() == null
                && reservaDto.getReservasPeriodoDtoList() == null
                && reservaDto.getReservaPuntualDto() == null
                && reservaDto.getReservaRecurrenteDto() == null
                && reservaDto.getPagosDtoList() == null
                && reservaDto.getTarifaDetallesDtoList() == null;
    }

    @Override
    @Transactional
    public ReservaDto aprobarReserva(String uuid, String aprobadoPor) {
        Reserva reserva = reservaRepository.findByUuidForRevision(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        String estadoAnterior = reserva.getEstadoReserva();
        String estadoValidacionAnterior = reserva.getEstadoValidacion();

        boolean recalcularPlazo = "puntual".equalsIgnoreCase(reserva.getTipoReserva())
                || "recurrente".equalsIgnoreCase(reserva.getTipoReserva());
        if (recalcularPlazo) {
            // A repeated approval must not restart the payment window or regress the state.
            if ("APROBADA".equalsIgnoreCase(estadoValidacionAnterior)
                    && !"SOLICITADA".equalsIgnoreCase(estadoAnterior)) {
                return ReservaMapper.toDto(reserva);
            }
            if (!"SOLICITADA".equalsIgnoreCase(estadoAnterior)) {
                throw new IllegalArgumentException("Solo se puede aprobar una reserva solicitada");
            }
            // The existing approval flow registers and saves the RUAT rate first.
            if (!Boolean.TRUE.equals(reserva.getEsReservaGratuita())
                    && (reserva.getNumeroTasa() == null || reserva.getNumeroTasa().isBlank())) {
                throw new IllegalArgumentException("Debe registrar la tasa en RUAT antes de aprobar la reserva");
            }
        }

        LocalDateTime fechaAprobacion = LocalDateTime.now(java.time.ZoneId.of("America/La_Paz"));
        if (recalcularPlazo) {
            // Discard the deadline from the original request, even when it already expired.
            reserva.setFechaLimitePago(null);
            if (Boolean.TRUE.equals(reserva.getEsReservaGratuita())) {
                reserva.setCancelacionAutomatica(false);
            } else {
                aplicarPoliticaPago(reserva, fechaAprobacion);
            }
        }
        reserva.setEstadoReserva("PENDIENTE");
        reserva.setEstadoValidacion("APROBADA");
        reserva.setAprobadoPor(aprobadoPor);
        reserva.setFechaAprobacion(fechaAprobacion);
        reserva.setFechaActualizacion(fechaAprobacion);

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
    public ReservaDto completarReservaEscuela(String uuid) {
        Reserva reserva = reservaRepository.findByUuidForRevision(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        if (!reserva.esRecurrenteGratuitaDeEscuela()) {
            throw new IllegalArgumentException("El cierre corresponde a una reserva recurrente gratuita de escuela municipal");
        }
        String anterior = reserva.getEstadoReserva();
        if ("COMPLETADA".equalsIgnoreCase(anterior)) return ReservaMapper.toDto(reserva);
        if (!"APROBADA".equalsIgnoreCase(reserva.getEstadoValidacion())
                || !("PENDIENTE".equalsIgnoreCase(anterior) || "EN_CURSO".equalsIgnoreCase(anterior)
                || "PAGADA".equalsIgnoreCase(anterior))) {
            throw new IllegalStateException("Solo se puede completar una reserva de escuela aprobada y no finalizada");
        }
        reserva.setEstadoReserva("COMPLETADA");
        reserva.setFechaActualizacion(LocalDateTime.now(java.time.ZoneId.of("America/La_Paz")));
        Reserva guardada = reservaRepository.save(reserva);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuario = authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal
                ? ((DeportesPrincipal) authentication.getPrincipal()).getSubject() : null;
        registrarAuditoria(guardada, "COMPLETE_SCHOOL", anterior, "COMPLETADA", usuario,
                "Cierre manual de reserva base: el usuario confirmó el registro de las mensualidades pendientes", null);
        return ReservaMapper.toDto(guardada);
    }

    /** Public list columns only. Never accept arbitrary entity paths from the client. */
    public static org.springframework.data.domain.Sort ordenListado(String columna, String direccion, String tipo) {
        boolean child = "puntual".equals(tipo) || "recurrente".equals(tipo);
        String prefix = child ? "reserva." : "";
        String key = columna == null ? "" : columna;
        if (key.startsWith("reservaDto.")) key = key.substring("reservaDto.".length());
        Map<String, String> common = Map.ofEntries(
                Map.entry("numeroReserva", "numeroReserva"), Map.entry("contribuyenteDto.nombreCompleto", "contribuyente.nombreCompletoOrden"),
                Map.entry("contribuyente", "contribuyente.nombreCompletoOrden"),
                Map.entry("campoDeportivoDto.complejoDto.nombre", "campoDeportivo.complejo.nombre"), Map.entry("complejo", "campoDeportivo.complejo.nombre"),
                Map.entry("campoDeportivoDto.nombre", "campoDeportivo.nombre"), Map.entry("campoDeportivo", "campoDeportivo.nombre"),
                Map.entry("tarifaDto.clasificadorRuatDto.nombre", "tarifa.clasificadorRuat.nombre"),
                Map.entry("tarifaDto.tipoCobro", "tarifa.tipoCobro"), Map.entry("tipoCobro", "tarifa.tipoCobro"),
                Map.entry("organizacionDto.nombre", "organizacion.nombre"), Map.entry("organizacion", "organizacion.nombre"),
                Map.entry("organizacionDto.tipoOrganizacion", "organizacion.tipoOrganizacion"),
                Map.entry("organizacionDto.disciplina", "organizacion.disciplina"), Map.entry("organizacionDto.entrenador", "organizacion.entrenador"),
                Map.entry("montoTotal", "montoTotal"), Map.entry("numeroTasa", "numeroTasa"), Map.entry("fechaLimitePago", "fechaLimitePago"),
                Map.entry("cancelacionAutomatica", "cancelacionAutomatica"), Map.entry("esReservaGratuita", "esReservaGratuita"),
                Map.entry("motivoGratuidad", "motivoGratuidad"), Map.entry("estadoValidacion", "estadoValidacion"),
                Map.entry("estadoReserva", "estadoReserva"), Map.entry("pagosDtoList[0].estadoPago", "estadoPagoOrden"),
                Map.entry("fechaCreacion", "fechaCreacion"));
        Set<String> puntual = Set.of("superficie", "categoria", "fechaUso", "horaInicio", "horaFin", "duracionHoras",
                "cantidadPersonas", "precioBaseAplicado", "observaciones", "fechaUsoReal", "periodo");
        Set<String> recurrente = Set.of("superficie", "categoria", "fechaInicio", "fechaFin", "diasSemana", "cantidadSesiones",
                "duracionSesionHoras", "cantidadPersonas", "precioBaseAplicado", "observaciones", "activo");
        String property = common.containsKey(key) ? prefix + common.get(key)
                : "puntual".equals(tipo) && puntual.contains(key) || "recurrente".equals(tipo) && recurrente.contains(key) ? key : null;
        if (property == null) property = "puntual".equals(tipo) ? "fechaUso" : "recurrente".equals(tipo) ? "fechaInicio" : "fechaCreacion";
        org.springframework.data.domain.Sort.Direction direction = "desc".equalsIgnoreCase(direccion) || child && !"asc".equalsIgnoreCase(direccion)
                ? org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        // Stable secondary order avoids repeated or skipped rows on equal values across pages.
        return org.springframework.data.domain.Sort.by(direction, property).and(org.springframework.data.domain.Sort.by(
                "puntual".equals(tipo) ? "idReservaPuntual" : "recurrente".equals(tipo) ? "idReservaRecurrente" : "idReserva"));
    }

    @Override
    @Transactional
    public ReservaDto actualizarEstadoUso(String uuid, String estado, String observacion) {
        if (!"NO_SHOW".equals(estado) && !"COMPLETADA".equals(estado)) {
            throw new IllegalArgumentException("Acción de uso no válida");
        }
        Reserva reserva = reservaRepository.findByUuidForRevision(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        if (!("puntual".equalsIgnoreCase(reserva.getTipoReserva()) || "recurrente".equalsIgnoreCase(reserva.getTipoReserva()))
                || reserva.esRecurrenteGratuitaDeEscuela()) {
            throw new IllegalArgumentException("Esta reserva no admite esta acción de uso. Las bases gratuitas de escuela tienen su propio cierre.");
        }
        String anterior = reserva.getEstadoReserva();
        if (estado.equals(anterior)) return ReservaMapper.toDto(reserva);
        if (!"APROBADA".equalsIgnoreCase(reserva.getEstadoValidacion())
                || !("PAGADA".equals(anterior) || "EN_CURSO".equals(anterior))) {
            throw new IllegalStateException("La reserva debe estar aprobada y PAGADA o EN_CURSO");
        }
        reserva.setEstadoReserva(estado);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String usuario = authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal
                ? ((DeportesPrincipal) authentication.getPrincipal()).getSubject() : null;
        Reserva guardada = reservaRepository.save(reserva);
        registrarAuditoria(guardada, "NO_SHOW".equals(estado) ? "NO_SHOW" : "COMPLETE_USE", anterior, estado,
                usuario, observacion, null);
        return ReservaMapper.toDto(guardada);
    }

    @Override
    @Transactional
    public ReservaDto rechazarReserva(String uuid, String motivoRechazo, String aprobadoPor) {
        validarMotivo(motivoRechazo, "El motivo de rechazo es requerido");
        Reserva reserva = reservaRepository.findByUuidForRevision(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        if (!"SOLICITADA".equals(reserva.getEstadoReserva())
                || !"PENDIENTE".equals(reserva.getEstadoValidacion())
                || "periodo".equalsIgnoreCase(reserva.getTipoReserva())
                || (reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank())) {
            throw new IllegalStateException("Solo se puede rechazar una solicitud pendiente de validación y sin tasa.");
        }
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
        var resultado = cancelarReserva(uuid, motivo, null, anuladoPor);
        if (!"CANCELADA".equals(resultado.getReservaDto().getEstadoReserva())) {
            throw new IllegalStateException(resultado.getMotivo());
        }
        return resultado.getReservaDto();
    }

    @Override
    @Transactional
    public com.gamq.deportes.dto.ReservaRevisionPage.Resultado cancelarReserva(
            String uuid, String motivo, String observacion, String anuladoPor) {
        validarMotivo(motivo, "El motivo de cancelación es requerido");
        Reserva reserva = reservaRepository.findByUuidForRevision(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
        String estadoAnterior = reserva.getEstadoReserva();

        var resultado = new com.gamq.deportes.dto.ReservaRevisionPage.Resultado()
                .setReservaDto(ReservaMapper.toDto(reserva)).setEstadoAnterior(estadoAnterior);
        if ("CANCELADA".equalsIgnoreCase(estadoAnterior)) return resultado;
        if (Set.of("SOLICITADA", "COMPLETADA", "EXPIRADA", "NO_SHOW").contains(estadoAnterior)) {
            throw new IllegalStateException("Este estado no admite anulación manual. Las solicitudes requieren rechazo administrativo.");
        }
        boolean tieneTasa = reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank();
        if (!tieneTasa && !Boolean.TRUE.equals(reserva.getEsReservaGratuita())) {
            throw new IllegalStateException("La reserva no tiene tasa para anular en RUAT. No se modificó su estado.");
        }
        if (tieneTasa) {
            if (!"PENDIENTE".equalsIgnoreCase(estadoAnterior)) {
                return resultado.setResultado("SIN_CAMBIOS")
                        .setDetalleLocal("Solo se puede anular una tasa de una reserva pendiente de pago.");
            }
            if (observacion == null || observacion.trim().length() <= 10) {
                throw new IllegalArgumentException("La observación de anulación debe tener más de 10 caracteres.");
            }
            if (!ruatRevisionService.verificarSinPagoParaAnular(reserva, resultado)) return resultado;
            var solicitud = new com.gamq.deportes.model.AnularTasa.AnularTasaRequest();
            solicitud.setNumeroTasa(reserva.getNumeroTasa());
            solicitud.setMotivoTasa(motivo);
            solicitud.setObservacion(observacion);
            var token = new com.gamq.deportes.model.AnularTasa.AnularTasaToken();
            token.setAnular(solicitud);
            var respuesta = apiService.anularTasa(token);
            var cuerpo = ruatRevisionService.registrarRespuesta(resultado, respuesta);
            if (!ruatRevisionService.confirmada(respuesta, cuerpo)) {
                return resultado.setResultado("ERROR").setRuat("NO_CONFIRMADO");
            }
            resultado.setRuat("ANULACION_CONFIRMADA");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        anuladoPor = authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal
                ? ((DeportesPrincipal) authentication.getPrincipal()).getSubject() : null;

        reserva.setEstadoReserva("CANCELADA");
        if (reserva.getReservasPeriodoList() != null) {
            reserva.getReservasPeriodoList().forEach(periodo -> periodo.setEstado(true));
        }
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
                ruatRevisionService.datosAuditoria(reservaCancelada, resultado, "ANULACION_MANUAL")
        );
        reservaNotificacionService.notificarCancelada(reservaCancelada);
        return resultado.setReservaDto(ReservaMapper.toDto(reservaCancelada)).setResultado("ACTUALIZADA");
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
            validarAccesoMensualidadParaContribuyente(inscripcion, reserva);
            periodo.setEstudianteEscuela(inscripcion);
            periodo.setMatricula(inscripcion.getMatricula());
            reserva.getReservasPeriodoList().add(periodo);
        }
    }

    private void actualizarDetallesPeriodo(
            Reserva reserva, List<com.gamq.deportes.dto.ReservaPeriodoDto> detallesDto) {
        Map<String, ReservaPeriodo> detallesExistentes = reserva.getReservasPeriodoList().stream()
                .filter(detalle -> detalle.getUuid() != null)
                .collect(Collectors.toMap(ReservaPeriodo::getUuid, detalle -> detalle, (first, second) -> first));
        List<com.gamq.deportes.dto.ReservaPeriodoDto> detallesNuevos = new ArrayList<>();

        for (com.gamq.deportes.dto.ReservaPeriodoDto detalleDto : detallesDto) {
            ReservaPeriodo detalleExistente = detalleDto.getUuid() == null
                    ? null
                    : detallesExistentes.get(detalleDto.getUuid());
            if (detalleExistente != null) {
                ReservaPeriodoMapper.updateEntityFromDto(detalleDto, detalleExistente);
            } else {
                detallesNuevos.add(detalleDto);
            }
        }

        if (!detallesNuevos.isEmpty()) {
            agregarDetallesPeriodo(reserva, detallesNuevos);
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
        return contribuyenteActualUuid() != null;
    }

    private String contribuyenteActualUuid() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof DeportesPrincipal)) {
            return null;
        }
        DeportesPrincipal principal = (DeportesPrincipal) authentication.getPrincipal();
        return "TAXPAYER".equalsIgnoreCase(principal.getUserType()) ? principal.getSubject() : null;
    }

    private void validarContribuyentePropio(Contribuyente contribuyente) {
        String actualUuid = contribuyenteActualUuid();
        if (actualUuid != null && (contribuyente == null || !actualUuid.equals(contribuyente.getUuid()))) {
            throw new IllegalArgumentException(
                    "Un contribuyente no puede registrar una reserva a nombre de otra persona");
        }
    }

    private void validarContribuyentePuedeGenerarMensualidad(Reserva reserva) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid == null) {
            return;
        }
        boolean esPagador = reserva.getContribuyente() != null
                && contribuyenteUuid.equals(reserva.getContribuyente().getUuid());
        boolean representaEscuela = reserva.getOrganizacion() != null
                && reserva.getOrganizacion().tieneRepresentante(contribuyenteUuid);
        if (!esPagador) {
            throw new IllegalArgumentException(
                    "Un contribuyente solo puede generar la mensualidad a su propio nombre");
        }
    }

    private void validarAccesoMensualidadParaContribuyente(EstudianteEscuela inscripcion, Reserva reserva) {
        String contribuyenteUuid = contribuyenteActualUuid();
        if (contribuyenteUuid == null) {
            return;
        }
        boolean esTutor = inscripcion.getContribuyenteDto() != null
                && contribuyenteUuid.equals(inscripcion.getContribuyenteDto().getUuid());
        boolean representaEscuela = reserva.getOrganizacion() != null
                && reserva.getOrganizacion().tieneRepresentante(contribuyenteUuid);
        if (!esTutor && !representaEscuela) {
            throw new IllegalArgumentException(
                    "No tiene autorización para registrar mensualidades de esta inscripción");
        }
    }

    private void aplicarPoliticaPago(Reserva reserva) {
        aplicarPoliticaPago(reserva, LocalDateTime.now(java.time.ZoneId.of("America/La_Paz")));
    }

    private void aplicarPoliticaPago(Reserva reserva, LocalDateTime inicioPlazo) {
        if ("periodo".equalsIgnoreCase(reserva.getTipoReserva())) {
            reserva.setCancelacionAutomatica(false);
            reserva.setFechaLimitePago(null);
            return;
        }
        if (reserva.getCancelacionAutomatica() == null) {
            reserva.setCancelacionAutomatica(true);
        }

        if (Boolean.TRUE.equals(reserva.getCancelacionAutomatica())) {
            reserva.setMotivoCancelacionAutomatica(null);
            reserva.setAutorizadoPor(null);
            if (reserva.getFechaLimitePago() == null) {
                reserva.setFechaLimitePago(inicioPlazo.plusHours(obtenerHorasValidacionPago(reserva)));
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
        auditoria.setDatosCambio(datosCambio == null ? buildReservaSnapshot(reserva) : normalizeJsonText(datosCambio));
        auditoria.setFechaCreacion(LocalDateTime.now());
        auditoria.setFechaAccion(LocalDateTime.now());
        // The local mutation and its audit must commit or roll back together.
        reservaAuditoriaRepository.save(auditoria);
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
                + "\",\"numeroTasa\":\"" + safeValue(reserva.getNumeroTasa())
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
