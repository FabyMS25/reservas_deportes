package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.dto.ReservaRecurrenteFilter;
import com.gamq.deportes.dto.mapper.ReservaRecurrenteMapper;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.ReservaAuditoriaRepository;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.repository.TableroRepository;
import com.gamq.deportes.service.ReservaRecurrenteService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservaRecurrenteServiceImpl implements ReservaRecurrenteService {
    private static final org.slf4j.Logger applicationLog = org.slf4j.LoggerFactory.getLogger(ReservaRecurrenteServiceImpl.class);
    @Autowired
    private com.gamq.deportes.security.ReservationListScope listScope;

    private static final String NOTIFICAR_RESERVA_ANULADA = "NOTIFICAR_RESERVA_ANULADA";

    @Autowired
    private ReservaRecurrenteRepository reservaRecurrenteRepository;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private ReservaAuditoriaRepository reservaAuditoriaRepository;
    @Autowired
    private TableroRepository tableroRepository;
    @Autowired
    private ReservaNotificacionService reservaNotificacionService;
    @Autowired
    private NotificacionDestinatariosReservaService destinatariosService;
    @Autowired
    private ReservaRuatRevisionService ruatRevisionService;

    @Override
    public List<ReservaRecurrenteDto> obtenerReservas() {
        List<ReservaRecurrente> reservas = reservaRecurrenteRepository.findAll();
        if (reservas.isEmpty()) {
            return List.of();
        }
        return reservas.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, true))
                .collect(Collectors.toList());
    }
    public Page<ReservaRecurrenteDto> obtenerReservas(Pageable pageable) {
        return reservaRecurrenteRepository.findAll(pageable)
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, true));
    }


    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public Page<ReservaRecurrenteDto> obtenerReservasConFiltros(ReservaRecurrenteFilter filter, Pageable pageable) {
        Specification<ReservaRecurrente> spec = listScope.children();
        final Specification<ReservaRecurrente> reviewScope = spec;
        if (filter.getTab() != null && filter.getTab() > 0) {
            String tabEstado = filter.getEstadoReservaFromTab();
            if (tabEstado != null) {
                if (tabEstado.contains(",")) {
                    // Multiple states (for tab 6)
                    List<String> estados = Arrays.asList(tabEstado.split(","));
                    spec = spec.and((root, query, cb) ->
                            root.get("reserva").get("estadoReserva").in(estados));
                } else {
                    // Single state
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.get("reserva").get("estadoReserva"), tabEstado));
                }
            }
        }
        if (filter.getEstadoReserva() != null && !filter.getEstadoReserva().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reserva").get("estadoReserva"), filter.getEstadoReserva()));
        }
        if (filter.getEstadoValidacion() != null && !filter.getEstadoValidacion().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("reserva").get("estadoValidacion"), filter.getEstadoValidacion()));
        }
        if (filter.getCategoria() != null && !filter.getCategoria().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("categoria")), "%" + filter.getCategoria().toLowerCase() + "%"));
        }
        if (filter.getConOrganizacion() != null) {
            spec = spec.and((root, query, cb) -> filter.getConOrganizacion()
                    ? cb.isNotNull(root.join("reserva", JoinType.LEFT).get("organizacion"))
                    : cb.isNull(root.join("reserva", JoinType.LEFT).get("organizacion")));
        }
        if (filter.getTipoContribuyente() != null && !filter.getTipoContribuyente().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("reserva", JoinType.LEFT).join("contribuyente", JoinType.LEFT)
                            .get("tipoContribuyente"), filter.getTipoContribuyente()));
        }
        if (filter.getTipoOrganizacion() != null && !filter.getTipoOrganizacion().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("reserva", JoinType.LEFT).join("organizacion", JoinType.LEFT)
                            .get("tipoOrganizacion"), filter.getTipoOrganizacion()));
        }
        if (filter.getTipoRecurrencia() != null && !filter.getTipoRecurrencia().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("tipoRecurrencia"), filter.getTipoRecurrencia()));
        }
        if (filter.getActivo() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("activo"), filter.getActivo()));
        }
        if (filter.getFromDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaInicio"), filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("fechaInicio"), filter.getToDate()));
        }
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            String searchTerm = "%" + filter.getSearchTerm().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                Join<ReservaRecurrente, Reserva> reservaJoin = root.join("reserva", JoinType.LEFT);
                Join<Reserva, Contribuyente> contribuyenteJoin = reservaJoin.join("contribuyente", JoinType.LEFT);
                Join<Reserva, CampoDeportivo> campoJoin = reservaJoin.join("campoDeportivo", JoinType.LEFT);
                Join<CampoDeportivo, ComplejoDeportivo> complejoJoin = campoJoin.join("complejo", JoinType.LEFT);
                Join<ReservaRecurrente, Tablero> tableroJoin = root.join("tablero", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(root.get("categoria")), searchTerm),
                        cb.like(cb.lower(root.get("superficie")), searchTerm),
                        cb.like(cb.lower(root.get("observaciones")), searchTerm),
                        cb.like(cb.lower(root.get("tipoRecurrencia")), searchTerm),
                        cb.like(cb.lower(root.get("diasSemana")), searchTerm),
                        // Reserva fields
                        cb.like(cb.lower(reservaJoin.get("numeroReserva")), searchTerm),
                        cb.like(cb.lower(reservaJoin.get("numeroTasa")), searchTerm),
                        // Contribuyente fields
                        cb.like(cb.lower(contribuyenteJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("primerApellido")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("segundoApellido")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("numeroDocumento")), searchTerm),
                        // CampoDeportivo and Complejo fields
                        cb.like(cb.lower(campoJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(complejoJoin.get("nombre")), searchTerm)
//                        cb.like(cb.lower(tableroJoin.get("horario")), searchTerm)
                );
            });
        }
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("fechaInicio"), Sort.Order.desc("idReservaRecurrente")));
        }
        final Specification<ReservaRecurrente> listSpec = spec;
        final Pageable listPage = pageable;
        if (Boolean.FALSE.equals(filter.getRevisarEstados())) {
            return ruatRevisionService.leerPagina(() -> reservaRecurrenteRepository.findAll(listSpec, listPage),
                    r -> ReservaRecurrenteMapper.toDto(r, true));
        }
        return ruatRevisionService.revisarLista(
                () -> reservaRecurrenteRepository.findAll(listSpec, listPage),
                r -> r.getReserva(), r -> ReservaRecurrenteMapper.toDto(r, true),
                after -> reservaRecurrenteRepository.findAll(
                        ReservaRuatRevisionService.candidatas(reviewScope, "reserva", after),
                        PageRequest.of(0, 100, Sort.by("reserva.idReserva"))));
    }
    @Override
    @Transactional(readOnly = true)
    public ReservaRecurrenteDto findByUuid(String uuid) {
        Optional<ReservaRecurrente> reservaRecurrenteOptional = reservaRecurrenteRepository.findByUuid(uuid);
        if (reservaRecurrenteOptional.isPresent()) {
            return ReservaRecurrenteMapper.toDto(reservaRecurrenteOptional.get(), true);
        } else {
            throw new NotFoundException(RESERVA_RECURRENTE, uuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaRecurrenteDto findByReservaUuid(String reservaUuid) {
        Optional<ReservaRecurrente> reservaRecurrenteOptional = reservaRecurrenteRepository.findByReservaUuid(reservaUuid);
        if (reservaRecurrenteOptional.isPresent()) {
            return ReservaRecurrenteMapper.toDto(reservaRecurrenteOptional.get(), false);
        } else {
            throw new NotFoundException(RESERVA_RECURRENTE, reservaUuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findByTipoRecurrencia(String tipoRecurrencia) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findByTipoRecurrencia(tipoRecurrencia);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findByActivo(Boolean activo) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findByActivo(activo);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findActivasEnFecha(LocalDate fecha) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findActivasEnFecha(fecha);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findActivasEnFechaPorCampo(String campoUuid, LocalDate fecha) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findActivasEnFechaPorCampo(campoUuid, fecha);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findActivasVigentes() {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findActivasVigentes();

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findActivasPorDiaSemana(String diaSemana, LocalDate fecha) {
        // First get all active reservations for the date
        List<ReservaRecurrente> reservasActivas = reservaRecurrenteRepository.findActivasEnFecha(fecha);

        // Then filter by day of week using Java
        return reservasActivas.stream()
                .filter(reserva -> {
                    String diasSemana = reserva.getDiasSemana();
                    if (diasSemana == null || diasSemana.isEmpty()) return false;

                    // Check if the day exists in the comma-separated string
                    String[] diasArray = diasSemana.split(",");
                    for (String dia : diasArray) {
                        if (dia.trim().equalsIgnoreCase(diaSemana.trim())) {
                            return true;
                        }
                    }
                    return false;
                })
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findPorVencer(LocalDate fechaLimite) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findPorVencer(fechaLimite);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReservaRecurrenteDto> findByCampoDeportivoUuid(String campoUuid, Pageable pageable) {
        return reservaRecurrenteRepository.findByCampoDeportivoUuid(campoUuid, pageable)
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false));
    }
    @Transactional(readOnly = true)
    public Long countActivasByCampoDeportivo(String campoUuid) {
        return reservaRecurrenteRepository.countActivasByCampoDeportivo(campoUuid);
    }
    @Transactional(readOnly = true)
    public Long countActivasVigentes() {
        return reservaRecurrenteRepository.countActivasVigentes();
    }

    @Override
    @Transactional
    public ReservaRecurrenteDto crearReservaRecurrente(ReservaRecurrenteDto reservaRecurrenteDto) {
        if (reservaRecurrenteDto == null) {
            throw new NullReferenceException(RESERVA_RECURRENTE);
        }

        // Validate parent reservation exists
        Reserva reserva = getReserva(reservaRecurrenteDto.getReservaDto().getUuid());

        ReservaRecurrente nuevaReservaRecurrente = ReservaRecurrenteMapper.toEntity(reservaRecurrenteDto, true);
        nuevaReservaRecurrente.setReserva(reserva);
        if (reservaRecurrenteDto.getTableroDto() != null && reservaRecurrenteDto.getTableroDto().getUuid() != null) {
            Tablero tablero = getTablero(reservaRecurrenteDto.getTableroDto().getUuid());
            nuevaReservaRecurrente.setTablero(tablero);
        }
        if (nuevaReservaRecurrente.getActivo() == null) {
            nuevaReservaRecurrente.setActivo(true);
        }
        if (nuevaReservaRecurrente.getFechaFin() != null &&
                nuevaReservaRecurrente.getFechaInicio().isAfter(nuevaReservaRecurrente.getFechaFin())) {
            throw new IllegalArgumentException("Fecha de inicio no puede ser después de la fecha de fin");
        }
        ReservaRecurrente reservaRecurrenteGuardada = reservaRecurrenteRepository.save(nuevaReservaRecurrente);
        registrarAuditoria(
                reservaRecurrenteGuardada.getReserva(),
                "CREATE_RECURRENTE",
                reserva.getEstadoReserva(),
                reserva.getEstadoReserva(),
                null,
                "Detalle de reserva recurrente creado",
                buildReservaRecurrenteSnapshot(reservaRecurrenteGuardada)
        );
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteGuardada, true);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED)
    public Page<ReservaRecurrenteDto> checkAndUpdateExpiredReservations() {
        return obtenerReservasConFiltros(new ReservaRecurrenteFilter(), PageRequest.of(0, 100));
    }

    @Override
    @Transactional
    public ReservaRecurrenteDto actualizarReservaRecurrente(ReservaRecurrenteDto reservaRecurrenteDto) {
        if (reservaRecurrenteDto == null) {
            throw new NullReferenceException(RESERVA_RECURRENTE);
        }

        Optional<ReservaRecurrente> reservaRecurrenteOptional = reservaRecurrenteRepository.findByUuid(reservaRecurrenteDto.getUuid());

        if (reservaRecurrenteOptional.isPresent()) {
            ReservaRecurrente reservaRecurrenteExistente = reservaRecurrenteOptional.get();
            if (reservaRecurrenteExistente.getReserva().getNumeroTasa() != null
                    && !reservaRecurrenteExistente.getReserva().getNumeroTasa().isBlank()) {
                throw new IllegalStateException("Una reserva recurrente con tasa no admite edición general");
            }
            LocalDate fechaInicioAnterior = reservaRecurrenteExistente.getFechaInicio();
            LocalDate fechaFinAnterior = reservaRecurrenteExistente.getFechaFin();
            String diasSemanaAnteriores = reservaRecurrenteExistente.getDiasSemana();
            String tableroAnterior = reservaRecurrenteExistente.getTablero() == null
                    ? null : reservaRecurrenteExistente.getTablero().getUuid();
            ReservaRecurrenteMapper.updateEntityFromDto(reservaRecurrenteDto, reservaRecurrenteExistente);
            if (reservaRecurrenteDto.getReservaDto() != null) {
                Reserva reserva = getReserva(reservaRecurrenteDto.getReservaDto().getUuid());
                reservaRecurrenteExistente.setReserva(reserva);
            }
            if (reservaRecurrenteDto.getTableroDto() != null) {
                Tablero tablero = getTablero(reservaRecurrenteDto.getTableroDto().getUuid());
                reservaRecurrenteExistente.setTablero(tablero);
            } else {
                reservaRecurrenteExistente.setTablero(null);
            }
            String tableroNuevo = reservaRecurrenteExistente.getTablero() == null
                    ? null : reservaRecurrenteExistente.getTablero().getUuid();
            boolean horarioCambiado =
                    !Objects.equals(fechaInicioAnterior, reservaRecurrenteExistente.getFechaInicio())
                    || !Objects.equals(fechaFinAnterior, reservaRecurrenteExistente.getFechaFin())
                    || !Objects.equals(diasSemanaAnteriores, reservaRecurrenteExistente.getDiasSemana())
                    || !Objects.equals(tableroAnterior, tableroNuevo);
            if (reservaRecurrenteDto.getFechaFin() != null &&
                    reservaRecurrenteDto.getFechaInicio().isAfter(reservaRecurrenteDto.getFechaFin())) {
                throw new IllegalArgumentException("Fecha de inicio no puede ser después de la fecha de fin");
            }
            ReservaRecurrente reservaRecurrenteActualizada = reservaRecurrenteRepository.save(reservaRecurrenteExistente);
            registrarAuditoria(
                    reservaRecurrenteActualizada.getReserva(),
                    "UPDATE_RECURRENTE",
                    reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                    reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                    null,
                    "Detalle de reserva recurrente actualizado",
                    buildReservaRecurrenteSnapshot(reservaRecurrenteActualizada)
            );
            if (horarioCambiado) {
                notificarCambioHorario(reservaRecurrenteActualizada);
            }
            return ReservaRecurrenteMapper.toDto(reservaRecurrenteActualizada, true);
        } else {
            throw new NotFoundException(RESERVA_RECURRENTE, reservaRecurrenteDto.getUuid());
        }
    }

    @Override
    @Transactional
    public ReservaRecurrenteDto activarReservaRecurrente(String uuid) {
        ReservaRecurrente reservaRecurrente = reservaRecurrenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_RECURRENTE, uuid));

        reservaRecurrente.setActivo(true);

        ReservaRecurrente reservaRecurrenteActualizada = reservaRecurrenteRepository.save(reservaRecurrente);
        registrarAuditoria(
                reservaRecurrenteActualizada.getReserva(),
                "ACTIVATE_RECURRENTE",
                reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                null,
                "Reserva recurrente activada",
                buildReservaRecurrenteSnapshot(reservaRecurrenteActualizada)
        );
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteActualizada, true);
    }

    private void notificarCambioHorario(ReservaRecurrente recurrente) {
        Reserva reserva = recurrente.getReserva();
        if (reserva == null || reserva.getContribuyente() == null) return;
        NotificacionDto comun = notificacionCambio(recurrente,
                ReservaNotificacionFormatter.mensaje(reserva, "El horario recurrente fue actualizado"));
        destinatariosService.publicar(
                "NOTIFICAR_CAMBIO_HORARIO",
                reserva,
                comun,
                comun,
                null);
    }

    private NotificacionDto notificacionCambio(ReservaRecurrente recurrente, String mensaje) {
        return new NotificacionDto()
                .setTipo("NOTIFICAR_CAMBIO_HORARIO")
                .setTitulo(ReservaNotificacionFormatter.titulo("Cambio de horario", recurrente.getReserva()))
                .setMensaje(mensaje)
                .setPrioridad("INFO")
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(recurrente.getUuid())
                .setAccionUrl("/reservations/recurrent");
    }

    @Override
    @Transactional
    public ReservaRecurrenteDto desactivarReservaRecurrente(String uuid) {
        ReservaRecurrente reservaRecurrente = reservaRecurrenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_RECURRENTE, uuid));

        reservaRecurrente.setActivo(false);

        ReservaRecurrente reservaRecurrenteActualizada = reservaRecurrenteRepository.save(reservaRecurrente);
        registrarAuditoria(
                reservaRecurrenteActualizada.getReserva(),
                "DEACTIVATE_RECURRENTE",
                reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                reservaRecurrenteActualizada.getReserva().getEstadoReserva(),
                null,
                "Reserva recurrente desactivada",
                buildReservaRecurrenteSnapshot(reservaRecurrenteActualizada)
        );
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteActualizada, true);
    }

    @Override
    @Transactional
    public String eliminarReserva(String uuid) {
        try {
        ReservaRecurrente reserva = reservaRecurrenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_RECURRENTE, uuid));
            registrarAuditoria(
                    reserva.getReserva(),
                    "DELETE_RECURRENTE",
                    reserva.getReserva().getEstadoReserva(),
                    reserva.getReserva().getEstadoReserva(),
                    null,
                    "Detalle de reserva recurrente desactivado",
                    buildReservaRecurrenteSnapshot(reserva)
            );
            reservaRecurrenteRepository.delete(reserva);
            return "Desactivación exitosa del RESERVA";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el RESERVA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReservaRecurrenteDto> findByCategoria(String categoria) {
        List<ReservaRecurrente> reservasRecurrentes = reservaRecurrenteRepository.findByCategoria(categoria);

        return reservasRecurrentes.stream()
                .map(reservaRecurrente -> ReservaRecurrenteMapper.toDto(reservaRecurrente, false))
                .collect(Collectors.toList());
    }

    private Reserva getReserva(String uuid) {
        return reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
    }

    private Tablero getTablero(String uuid) {
        return tableroRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TABLERO, uuid));
    }

    private void registrarAuditoria(Reserva reserva, String accion, String estadoAnterior, String estadoNuevo,
                                    String usuarioUuid, String observaciones, String datosCambio) {
        if (reserva == null) {
            return;
        }
        ReservaAuditoria auditoria = new ReservaAuditoria();
        auditoria.setReserva(reserva);
        auditoria.setAccion(accion);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setUsuarioUuid(usuarioUuid);
        auditoria.setObservaciones(observaciones);
        auditoria.setDatosCambio(datosCambio);
        auditoria.setFechaAccion(LocalDateTime.now());
        reservaAuditoriaRepository.save(auditoria);
    }

    private String buildReservaRecurrenteSnapshot(ReservaRecurrente reservaRecurrente) {
        return "{\"uuid\":\"" + safeValue(reservaRecurrente.getUuid())
                + "\",\"tipoRecurrencia\":\"" + safeValue(reservaRecurrente.getTipoRecurrencia())
                + "\",\"fechaInicio\":\"" + safeValue(reservaRecurrente.getFechaInicio())
                + "\",\"fechaFin\":\"" + safeValue(reservaRecurrente.getFechaFin())
                + "\",\"cantidadSesiones\":\"" + safeValue(reservaRecurrente.getCantidadSesiones())
                + "\",\"duracionSesionHoras\":\"" + safeValue(reservaRecurrente.getDuracionSesionHoras())
                + "\",\"precioBaseAplicado\":\"" + safeValue(reservaRecurrente.getPrecioBaseAplicado())
                + "\",\"tarifaMensual\":\"" + safeValue(reservaRecurrente.getTarifaMensual())
                + "\",\"activo\":\"" + safeValue(reservaRecurrente.getActivo()) + "\"}";
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }

}
