package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaPuntualFilter;
import com.gamq.deportes.dto.mapper.ReservaMapper;
import com.gamq.deportes.dto.mapper.ReservaPuntualMapper;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.ReservaPuntualRepository;
import com.gamq.deportes.repository.ReservaAuditoriaRepository;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.service.ReservaPuntualService;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class ReservaPuntualServiceImpl implements ReservaPuntualService {
    private static final org.slf4j.Logger applicationLog = org.slf4j.LoggerFactory.getLogger(ReservaPuntualServiceImpl.class);
    @Autowired
    private com.gamq.deportes.security.ReservationListScope listScope;


    private static final String NOTIFICAR_RESERVA_ANULADA = "NOTIFICAR_RESERVA_ANULADA";

    @Autowired
    private ReservaPuntualRepository reservaPuntualRepository;

    @Autowired
    private ReservaRecurrenteRepository reservaRecurrenteRepository;
    @Autowired
    private ReservaRepository reservaRepository;
    @Autowired
    private ReservaAuditoriaRepository reservaAuditoriaRepository;
    @Autowired
    private ReservaNotificacionService reservaNotificacionService;

    @Autowired
    private NotificacionDestinatariosReservaService destinatariosService;

    public Page<ReservaPuntualDto> obtenerReservasConFiltros(ReservaPuntualFilter filter, Pageable pageable) {
        Specification<ReservaPuntual> spec = listScope.children();
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
        if (filter.getTipoBeneficiario() != null && !filter.getTipoBeneficiario().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<ReservaPuntual, Reserva> reservaJoin = root.join("reserva", JoinType.LEFT);
                if ("ORGANIZACION".equalsIgnoreCase(filter.getTipoBeneficiario())) {
                    return cb.isNotNull(reservaJoin.get("organizacion"));
                }
                if ("CONTRIBUYENTE".equalsIgnoreCase(filter.getTipoBeneficiario())) {
                    return cb.and(cb.isNotNull(reservaJoin.get("contribuyente")),
                            cb.isNull(reservaJoin.get("organizacion")));
                }
                return cb.conjunction();
            });
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
        if (filter.getFromDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaUso"), filter.getFromDate()));
        }
        if (filter.getToDate() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("fechaUso"), filter.getToDate()));
        }
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            String searchTerm = "%" + filter.getSearchTerm().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                Join<ReservaPuntual, Reserva> reservaJoin = root.join("reserva", JoinType.LEFT);
                Join<Reserva, Contribuyente> contribuyenteJoin = reservaJoin.join("contribuyente", JoinType.LEFT);
                Join<Reserva, CampoDeportivo> campoJoin = reservaJoin.join("campoDeportivo", JoinType.LEFT);
                Join<CampoDeportivo, ComplejoDeportivo> complejoJoin = campoJoin.join("complejo", JoinType.LEFT);
                return cb.or(
                        cb.like(cb.lower(root.get("categoria")), searchTerm),
                        cb.like(cb.lower(root.get("superficie")), searchTerm),
                        cb.like(cb.lower(root.get("observaciones")), searchTerm),
                        cb.like(cb.lower(reservaJoin.get("numeroReserva")), searchTerm),
                        cb.like(cb.lower(reservaJoin.get("numeroTasa")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("primerApellido")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("segundoApellido")), searchTerm),
                        cb.like(cb.lower(contribuyenteJoin.get("numeroDocumento")), searchTerm),
                        cb.like(cb.lower(campoJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(complejoJoin.get("nombre")), searchTerm)
                );
            });
        }
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("fechaUso"), Sort.Order.desc("idReservaPuntual")));
        }
        Page<ReservaPuntual> reservasPage = reservaPuntualRepository.findAll(spec, pageable);
        return reservasPage.map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, true));
    }

    @Override
    public List<ReservaPuntualDto> obtenerReservas() {
        List<ReservaPuntual> reservas = reservaPuntualRepository.findAll();
        if (reservas.isEmpty()) {
            return List.of();
        }
        return reservas.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, true))
                .collect(Collectors.toList());
    }

    public Page<ReservaPuntualDto> obtenerReservas(Pageable pageable) {
        return reservaPuntualRepository.findAll(pageable)
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, true));
    }

    @Override
    @Transactional
    public List<ReservaPuntualDto> checkAndUpdateExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<ReservaPuntual> expiredReservas = reservaPuntualRepository
                .findExpiredPendingReservations(now, "PENDIENTE");

        List<ReservaPuntualDto> updatedReservas = new ArrayList<>();

        for (ReservaPuntual reserva : expiredReservas) {
            try {

                // Store original state for comparison
                String originalEstado = reserva.getReserva().getEstadoReserva();

                reserva.getReserva().setEstadoReserva("EXPIRADA");
                reserva.getReserva().setAnuladoPor(null);
                reserva.getReserva().setMotivoRechazo("NO SE EFECTUO EL PAGO EN EL DIA DE REGISTRO");
                reserva.getReserva().setFechaRechazo(LocalDateTime.now());

                Reserva updatedReserva = reservaRepository.save(reserva.getReserva());
                reserva.setReserva(updatedReserva);

                if (!originalEstado.equals("EXPIRADA")) {
                    registrarAuditoria(
                            updatedReserva,
                            "EXPIRE",
                            originalEstado,
                            updatedReserva.getEstadoReserva(),
                            null,
                            "Reserva puntual expirada por falta de pago",
                            null
                    );
                    reservaNotificacionService.notificarExpirada(updatedReserva);
                    updatedReservas.add(ReservaPuntualMapper.toDto(reserva, true));
                }
            } catch (Exception e) {
                applicationLog.error("Failed to expire reservation {}", reserva.getReserva().getNumeroReserva(), e);
            }
        }
        return updatedReservas;
    }


    @Override
    @Transactional(readOnly = true)
    public ReservaPuntualDto findByUuid(String uuid) {
        Optional<ReservaPuntual> reservaPuntualOptional = reservaPuntualRepository.findByUuid(uuid);
        if (reservaPuntualOptional.isPresent()) {
            return ReservaPuntualMapper.toDto(reservaPuntualOptional.get(), true);
        } else {
            throw new NotFoundException(RESERVA_PUNTUAL, uuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaPuntualDto findByReservaUuid(String reservaUuid) {
        Optional<ReservaPuntual> reserva =  reservaPuntualRepository.findByReservaUuid(reservaUuid);
        if (reserva.isPresent()) {
            return ReservaPuntualMapper.toDto(reserva.get(), false);
        } else {
            throw new NotFoundException(RESERVA_PUNTUAL, reservaUuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findByFechaUso(LocalDate fechaUso) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findByFechaUso(fechaUso);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findByFechaUsoBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findByFechaUsoBetween(fechaInicio, fechaFin);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findByCampoDeportivoAndFechaUso(String campoUuid, LocalDate fechaUso) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findByCampoDeportivoAndFechaUso(campoUuid, fechaUso);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findReservasActivasPorCampoYFecha(String campoUuid, LocalDate fechaUso) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findReservasActivasPorCampoYFecha(campoUuid, fechaUso);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

//    @Transactional(readOnly = true)
//    public List<ReservaPuntualDto> findReservasConflictivas(String campoUuid, LocalDate fechaUso, LocalTime horaInicio, LocalTime horaFin, String excludeReservaUuid) {
//        List<ReservaPuntual> reservasConflictivas = reservaPuntualRepository.findReservasConflictivas(
//                campoUuid, fechaUso, horaInicio, horaFin, excludeReservaUuid);
//
//        return reservasConflictivas.stream()
//                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
//                .collect(Collectors.toList());
//    }
    @Override
    @Transactional(readOnly = true)
    public boolean existeReservaConflictiva(String campoUuid,
                                            LocalDate fechaUso,
                                            LocalTime horaInicio,
                                            LocalTime horaFin,
                                            String excludeReservaUuid) {
        boolean conflictoPuntual = !reservaPuntualRepository.findReservasConflictivas(
                campoUuid, fechaUso, horaInicio, horaFin, excludeReservaUuid
        ).isEmpty();

        if (conflictoPuntual) {
            return true;
        }

        List<ReservaRecurrente> recurrentes = reservaRecurrenteRepository.findActivasEnFechaPorCampoEntity(campoUuid, fechaUso);

        for (ReservaRecurrente recurrente : recurrentes) {
            if (recurrente.getReserva() == null) {
                continue;
            }

            if (excludeReservaUuid != null && excludeReservaUuid.equals(recurrente.getReserva().getUuid())) {
                continue;
            }

            if (hasConflictoConTablero(recurrente, fechaUso, horaInicio, horaFin)) {
                return true;
            }
        }

        return false;
    }


    private boolean hasOverlap(LocalTime existingStart,
                               LocalTime existingEnd,
                               LocalTime newStart,
                               LocalTime newEnd) {
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private String mapDiaSemana(String dayOfWeek) {
        switch (dayOfWeek) {
            case "MONDAY": return "LUNES";
            case "TUESDAY": return "MARTES";
            case "WEDNESDAY": return "MIERCOLES";
            case "THURSDAY": return "JUEVES";
            case "FRIDAY": return "VIERNES";
            case "SATURDAY": return "SABADO";
            case "SUNDAY": return "DOMINGO";
            default: return dayOfWeek;
        }
    }
    private boolean hasConflictoConTablero(ReservaRecurrente recurrente,
                                           LocalDate fechaUso,
                                           LocalTime nuevaHoraInicio,
                                           LocalTime nuevaHoraFin) {
        if (recurrente.getTablero() == null || recurrente.getTablero().getHorario() == null) {
            return false;
        }

        if (recurrente.getFechaInicio() == null || recurrente.getFechaFin() == null) {
            return false;
        }

        if (fechaUso.isBefore(recurrente.getFechaInicio()) || fechaUso.isAfter(recurrente.getFechaFin())) {
            return false;
        }

        String[][][] horario = recurrente.getTablero().getHorario();

        int monthIndex =
                (fechaUso.getYear() - recurrente.getFechaInicio().getYear()) * 12
                        + (fechaUso.getMonthValue() - recurrente.getFechaInicio().getMonthValue());

        if (monthIndex < 0 || monthIndex >= horario.length) {
            return false;
        }

        String[][] monthMatrix = horario[monthIndex];
        if (monthMatrix == null) {
            return false;
        }

        int dayIndex = fechaUso.getDayOfMonth() - 1;
        if (dayIndex < 0 || dayIndex >= monthMatrix.length) {
            return false;
        }

        String[] slots = monthMatrix[dayIndex];
        if (slots == null) {
            return false;
        }

        for (String slot : slots) {
            if (slot == null || slot.isBlank() || !slot.contains("-")) {
                continue;
            }

            String[] parts = slot.split("-");
            if (parts.length != 2) {
                continue;
            }

            LocalTime existenteInicio = LocalTime.parse(parts[0].trim());
            LocalTime existenteFin = LocalTime.parse(parts[1].trim());

            if (existeSolapamiento(existenteInicio, existenteFin, nuevaHoraInicio, nuevaHoraFin)) {
                return true;
            }
        }

        return false;
    }
    private boolean existeSolapamiento(LocalTime existenteInicio,
                                       LocalTime existenteFin,
                                       LocalTime nuevaInicio,
                                       LocalTime nuevaFin) {
        return existenteInicio.isBefore(nuevaFin) && existenteFin.isAfter(nuevaInicio);
    }

//    @Transactional(readOnly = true)
//    public boolean existeReservaConflictiva(String campoUuid, LocalDate fechaUso, LocalTime horaInicio, LocalTime horaFin, String excludeReservaUuid) {
//        List<ReservaPuntual> reservasConflictivas = reservaPuntualRepository.findReservasConflictivas(
//                campoUuid, fechaUso, horaInicio, horaFin, excludeReservaUuid);
//        return !reservasConflictivas.isEmpty();
//    }
    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findReservasConflictivas(String campoUuid,
                                                            LocalDate fechaUso,
                                                            LocalTime horaInicio,
                                                            LocalTime horaFin,
                                                            String excludeReservaUuid) {
        List<ReservaPuntual> reservasConflictivas = reservaPuntualRepository.findReservasConflictivas(
                campoUuid, fechaUso, horaInicio, horaFin, excludeReservaUuid
        );

        return reservasConflictivas.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Page<ReservaPuntualDto> findByCampoDeportivoUuid(String campoUuid, Pageable pageable) {
        return reservaPuntualRepository.findByCampoDeportivoUuid(campoUuid, pageable)
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false));
    }

    @Transactional(readOnly = true)
    public Long countByCampoDeportivoAndFechaUso(String campoUuid, LocalDate fechaUso) {
        return reservaPuntualRepository.countByCampoDeportivoAndFechaUso(campoUuid, fechaUso);
    }

    @Override
    @Transactional
    public ReservaPuntualDto crearReservaPuntual(ReservaPuntualDto reservaPuntualDto) {
        if (reservaPuntualDto == null) {
            throw new NullReferenceException(RESERVA_PUNTUAL);
        }
        Reserva reserva = getReserva(reservaPuntualDto.getReservaDto().getUuid());

        ReservaPuntual nuevaReservaPuntual = ReservaPuntualMapper.toEntity(reservaPuntualDto, true);
        nuevaReservaPuntual.setReserva(reserva);
        if (nuevaReservaPuntual.getDuracionHoras() == null &&
                nuevaReservaPuntual.getHoraInicio() != null &&
                nuevaReservaPuntual.getHoraFin() != null) {
            int duracion = nuevaReservaPuntual.getHoraFin().getHour() - nuevaReservaPuntual.getHoraInicio().getHour();
            nuevaReservaPuntual.setDuracionHoras(Math.max(duracion, 1));
        }

        ReservaPuntual reservaPuntualGuardada = reservaPuntualRepository.save(nuevaReservaPuntual);
        registrarAuditoria(
                reservaPuntualGuardada.getReserva(),
                "CREATE_PUNTUAL",
                reserva.getEstadoReserva(),
                reserva.getEstadoReserva(),
                null,
                "Detalle de reserva puntual creado",
                buildReservaPuntualSnapshot(reservaPuntualGuardada)
        );
        return ReservaPuntualMapper.toDto(reservaPuntualGuardada, true);
    }

    @Override
    @Transactional
    public ReservaPuntualDto actualizarReservaPuntual(ReservaPuntualDto reservaPuntualDto) {
        if (reservaPuntualDto == null) {
            throw new NullReferenceException(RESERVA_PUNTUAL);
        }

        Optional<ReservaPuntual> reservaPuntualOptional = reservaPuntualRepository.findByUuid(reservaPuntualDto.getUuid());

        if (reservaPuntualOptional.isPresent()) {
            ReservaPuntual reservaPuntualExistente = reservaPuntualOptional.get();
            LocalDate fechaUsoAnterior = reservaPuntualExistente.getFechaUso();
            LocalTime horaInicioAnterior = reservaPuntualExistente.getHoraInicio();
            LocalTime horaFinAnterior = reservaPuntualExistente.getHoraFin();

            // Update fields
            ReservaPuntualMapper.updateEntityFromDto(reservaPuntualDto, reservaPuntualExistente);

            // Update parent reservation if provided
            if (reservaPuntualDto.getReservaDto() != null) {
                Reserva reserva = getReserva(reservaPuntualDto.getReservaDto().getUuid());
                reservaPuntualExistente.setReserva(reserva);
            }

            // Recalculate duration if times changed
            if (reservaPuntualDto.getHoraInicio() != null && reservaPuntualDto.getHoraFin() != null) {
                int duracion = reservaPuntualDto.getHoraFin().getHour() - reservaPuntualDto.getHoraInicio().getHour();
                reservaPuntualExistente.setDuracionHoras(Math.max(duracion, 1));
            }

            ReservaPuntual reservaPuntualActualizada = reservaPuntualRepository.save(reservaPuntualExistente);
            boolean horarioCambiado = !Objects.equals(fechaUsoAnterior, reservaPuntualActualizada.getFechaUso())
                    || !Objects.equals(horaInicioAnterior, reservaPuntualActualizada.getHoraInicio())
                    || !Objects.equals(horaFinAnterior, reservaPuntualActualizada.getHoraFin());
            registrarAuditoria(
                    reservaPuntualActualizada.getReserva(),
                    "UPDATE_PUNTUAL",
                    reservaPuntualActualizada.getReserva().getEstadoReserva(),
                    reservaPuntualActualizada.getReserva().getEstadoReserva(),
                    null,
                    "Detalle de reserva puntual actualizado",
                    buildReservaPuntualSnapshot(reservaPuntualActualizada)
            );
            if (horarioCambiado) {
                notificarCambioHorario(reservaPuntualActualizada, fechaUsoAnterior, horaInicioAnterior, horaFinAnterior);
            }
            return ReservaPuntualMapper.toDto(reservaPuntualActualizada, true);
        } else {
            throw new NotFoundException(RESERVA_PUNTUAL, reservaPuntualDto.getUuid());
        }
    }

    @Override
    @Transactional
    public ReservaPuntualDto registrarUsoReal(String uuid, LocalDateTime fechaUsoReal) {
        ReservaPuntual reservaPuntual = reservaPuntualRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_PUNTUAL, uuid));

        reservaPuntual.setFechaUsoReal(fechaUsoReal);

        ReservaPuntual reservaPuntualActualizada = reservaPuntualRepository.save(reservaPuntual);
        registrarAuditoria(
                reservaPuntualActualizada.getReserva(),
                "REGISTER_REAL_USE",
                reservaPuntualActualizada.getReserva().getEstadoReserva(),
                reservaPuntualActualizada.getReserva().getEstadoReserva(),
                null,
                "Uso real registrado para reserva puntual",
                "{\"fechaUsoReal\":\"" + safeValue(fechaUsoReal) + "\"}"
        );
        return ReservaPuntualMapper.toDto(reservaPuntualActualizada, true);
    }

    @Override
    @Transactional
    public String eliminarReserva(String uuid) {
        try {
            ReservaPuntual reserva = reservaPuntualRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_PUNTUAL, uuid));

            registrarAuditoria(
                    reserva.getReserva(),
                    "DELETE_PUNTUAL",
                    reserva.getReserva().getEstadoReserva(),
                    reserva.getReserva().getEstadoReserva(),
                    null,
                    "Detalle de reserva puntual desactivado",
                    buildReservaPuntualSnapshot(reserva)
            );
            reservaPuntualRepository.delete(reserva);

            return "Desactivación exitosa del RESERVA";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el RESERVA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }
    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findByCategoria(String categoria) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findByCategoria(categoria);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaPuntualDto> findByCantidadPersonasBetween(Integer cantidadMin, Integer cantidadMax) {
        List<ReservaPuntual> reservasPuntuales = reservaPuntualRepository.findByCantidadPersonasBetween(cantidadMin, cantidadMax);

        return reservasPuntuales.stream()
                .map(reservaPuntual -> ReservaPuntualMapper.toDto(reservaPuntual, false))
                .collect(Collectors.toList());
    }

    private Reserva getReserva(String uuid) {
        return reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
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

    private String buildReservaPuntualSnapshot(ReservaPuntual reservaPuntual) {
        return "{\"uuid\":\"" + safeValue(reservaPuntual.getUuid())
                + "\",\"fechaUso\":\"" + safeValue(reservaPuntual.getFechaUso())
                + "\",\"horaInicio\":\"" + safeValue(reservaPuntual.getHoraInicio())
                + "\",\"horaFin\":\"" + safeValue(reservaPuntual.getHoraFin())
                + "\",\"duracionHoras\":\"" + safeValue(reservaPuntual.getDuracionHoras())
                + "\",\"cantidadPersonas\":\"" + safeValue(reservaPuntual.getCantidadPersonas())
                + "\",\"precioBaseAplicado\":\"" + safeValue(reservaPuntual.getPrecioBaseAplicado()) + "\"}";
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }

    private void notificarCambioHorario(ReservaPuntual reservaPuntual,
                                        LocalDate fechaUsoAnterior,
                                        LocalTime horaInicioAnterior,
                                        LocalTime horaFinAnterior) {
        Reserva reserva = reservaPuntual.getReserva();
        if (reserva == null || reserva.getContribuyente() == null || reserva.getContribuyente().getUuid() == null) {
            return;
        }

        String horarioAnterior = formatHorario(fechaUsoAnterior, horaInicioAnterior, horaFinAnterior);
        String horarioNuevo = formatHorario(reservaPuntual.getFechaUso(), reservaPuntual.getHoraInicio(), reservaPuntual.getHoraFin());
        NotificacionDto comun = new NotificacionDto()
                .setTipo("NOTIFICAR_CAMBIO_HORARIO")
                .setTitulo(ReservaNotificacionFormatter.titulo("Cambio de horario", reserva))
                .setMensaje(ReservaNotificacionFormatter.mensaje(reserva,
                        "El horario cambió de " + horarioAnterior + " a " + horarioNuevo))
                .setPrioridad("INFO")
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(reservaPuntual.getUuid())
                .setAccionUrl(ReservaNotificacionFormatter.ruta(reserva))
                .setMetadataJson("{\"horarioAnterior\":\"" + horarioAnterior
                        + "\",\"horarioNuevo\":\"" + horarioNuevo + "\"}");
        destinatariosService.publicar(
                "NOTIFICAR_CAMBIO_HORARIO", reserva, comun, comun, null);
    }

    private String formatHorario(LocalDate fechaUso, LocalTime horaInicio, LocalTime horaFin) {
        return safeValue(fechaUso) + " " + safeValue(horaInicio) + "-" + safeValue(horaFin);
    }

    private String obtenerRutaReserva(Reserva reserva) {
        return "recurrente".equalsIgnoreCase(reserva.getTipoReserva())
                ? "/reservations/recurrent"
                : "/reservations/one-time";
    }

}
