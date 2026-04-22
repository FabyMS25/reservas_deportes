package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.dto.ReservaPeriodoFilter;
import com.gamq.deportes.dto.mapper.CampoDeportivoMapper;
import com.gamq.deportes.dto.mapper.ReservaPeriodoMapper;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.ReservaPeriodoRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.service.ReservaPeriodoService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.RESERVA_PERIODO;
import static com.gamq.deportes.exception.enums.EntityType.RESERVA;

@Component
public class ReservaPeriodoServiceImpl implements ReservaPeriodoService {

    @Autowired
    private ReservaPeriodoRepository reservaPeriodoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Override
    public List<ReservaPeriodoDto> obtenerTodos() {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findAll();
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }
    @Override
    public List<ReservaPeriodoDto> findByEstudianteUuid(String estudianteUuid) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByEstudianteUuid(estudianteUuid);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }
    @Override
    public Page<ReservaPeriodoDto> obtenerReservasConFiltros(ReservaPeriodoFilter filter, Pageable pageable) {
        Specification<ReservaPeriodo> spec = Specification.where(null);
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        // Apply tab filtering FIRST (before other filters)
        if (filter.getTab() != null && filter.getTab() > 0) {
            switch (filter.getTab()) {
                case 1: // SOLICITADA
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), "SOLICITADA"));
                    break;

                case 2: // PENDIENTE
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), "PENDIENTE"));
                    break;

                case 3: // PAGADA
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), "PAGADA"));
                    break;

                case 4: // Mes en Curso (EN_CURSO) - Filter by current month/year
                    spec = spec.and((root, query, cb) ->
                            cb.and(
                                    cb.equal(root.get("año"), currentYear),
                                    cb.equal(root.get("mes"), currentMonth),
                                    cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), "EN_CURSO")
                            ));
                    break;

                case 5: // COMPLETADA
                    spec = spec.and((root, query, cb) ->
                            cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), "COMPLETADA"));
                    break;

                case 6: // Canceladas/Expiradas (multiple states)
                    List<String> estados = Arrays.asList("CANCELADA", "EXPIRADA", "NO_SHOW", "RECHAZADA");
                    spec = spec.and((root, query, cb) ->
                            root.join("reserva", JoinType.LEFT).get("estadoReserva").in(estados));
                    break;

                default:
                    // Tab 0 - Todas, no filter needed
                    break;
            }
        }
        if (filter.getEstadoReserva() != null && !filter.getEstadoReserva().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("reserva", JoinType.LEFT).get("estadoReserva"), filter.getEstadoReserva()));
        }
        if (filter.getEstadoValidacion() != null && !filter.getEstadoValidacion().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("reserva", JoinType.LEFT).get("estadoValidacion"), filter.getEstadoValidacion()));
        }
        if (filter.getCategoria() != null && !filter.getCategoria().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("categoria")), "%" + filter.getCategoria().toLowerCase() + "%"));
        }
        if (filter.getEscuelaFiltro() != null && !filter.getEscuelaFiltro().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<ReservaPeriodo, Reserva> reservaJoin = root.join("reserva", JoinType.LEFT);
                Join<Reserva, Organizacion> organizacionJoin = reservaJoin.join("organizacion", JoinType.LEFT);
                return cb.equal(organizacionJoin.get("uuid"), filter.getEscuelaFiltro());
            });
        }
        if (filter.getPeriodoFiltro() != null && !filter.getPeriodoFiltro().isEmpty()) {
            String[] parts = filter.getPeriodoFiltro().split("-");
            if (parts.length == 2) {
                int año = Integer.parseInt(parts[0]);
                int mes = Integer.parseInt(parts[1]);
                spec = spec.and((root, query, cb) ->
                        cb.and(
                                cb.equal(root.get("año"), año),
                                cb.equal(root.get("mes"), mes)
                        ));
            }
        }
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
            String searchTerm = "%" + filter.getSearchTerm().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                Join<ReservaPeriodo, Reserva> reservaJoin = root.join("reserva", JoinType.LEFT);
                Join<Reserva, EstudianteEscuela> estudianteJoin = reservaJoin.join("estudianteEscuela", JoinType.LEFT);
                Join<Reserva, Organizacion> organizacionJoin = reservaJoin.join("organizacion", JoinType.LEFT);
                Join<Reserva, CampoDeportivo> campoJoin = reservaJoin.join("campoDeportivo", JoinType.LEFT);
                Join<CampoDeportivo, ComplejoDeportivo> complejoJoin = campoJoin.join("complejo", JoinType.LEFT);
                return cb.or(
                        // Estudiante fields - FIXED: nombreCompleto is transient, use individual fields
                        cb.like(cb.lower(estudianteJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(estudianteJoin.get("primerApellido")), searchTerm),
                        cb.like(cb.lower(estudianteJoin.get("segundoApellido")), searchTerm),
                        cb.like(cb.lower(estudianteJoin.get("matricula")), searchTerm),
                        cb.like(cb.lower(estudianteJoin.get("numeroDocumento")), searchTerm),
                        // Organización fields
                        cb.like(cb.lower(organizacionJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(organizacionJoin.get("disciplina")), searchTerm),
                        cb.like(cb.lower(organizacionJoin.get("entrenador")), searchTerm),
                        // Reserva fields
                        cb.like(cb.lower(reservaJoin.get("numeroReserva")), searchTerm),
                        cb.like(cb.lower(reservaJoin.get("numeroTasa")), searchTerm),
                        cb.like(cb.lower(reservaJoin.get("montoTotal").as(String.class)), searchTerm),
                        // Campo and Complejo fields
                        cb.like(cb.lower(campoJoin.get("nombre")), searchTerm),
                        cb.like(cb.lower(complejoJoin.get("nombre")), searchTerm)
                );
            });
        }
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by("fechaCreacion").descending());
        }
        Page<ReservaPeriodo> reservasPage = reservaPeriodoRepository.findAll(spec, pageable);
        return reservasPage.map(reserva -> ReservaPeriodoMapper.toDto(reserva, true));
    }
     @Override
    public ReservaPeriodoDto findByUuid(String uuid) {
        Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByUuid(uuid);
        if (reservaPeriodoOptional.isPresent()) {
            return ReservaPeriodoMapper.toDto(reservaPeriodoOptional.get(),true);
        } else {
            throw new NotFoundException(RESERVA_PERIODO, uuid);
        }
    }

    @Override
    public ReservaPeriodoDto findByReservaUuid(String reservaUuid) {
        Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByReservaUuid(reservaUuid);
        if (reservaPeriodoOptional.isPresent()) {
            return ReservaPeriodoMapper.toDto(reservaPeriodoOptional.get(),false);
        } else {
            throw new NotFoundException(RESERVA_PERIODO, reservaUuid);
        }
    }

    @Override
    public List<ReservaPeriodoDto> findByPeriodo(String periodo) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByPeriodo(periodo);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }


    @Override
    public List<ReservaPeriodoDto> findByContribuyenteUuid(String contribuyenteUuid) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByContribuyenteUuid(contribuyenteUuid);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }

    @Override
    public List<ReservaPeriodoDto> findByOrganizacionUuid(String organizacionUuid) {
        List<ReservaPeriodo> reservasPeriodo = reservaPeriodoRepository.findByOrganizacionUuid(organizacionUuid);
        if (!reservasPeriodo.isEmpty()) {
            return reservasPeriodo.stream()
                    .map(reserva -> ReservaPeriodoMapper.toDto(reserva, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(RESERVA_PERIODO);
        }
    }


    @Override
    @Transactional
    public ReservaPeriodoDto crearReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto) {
        if (reservaPeriodoDto != null) {
            // Verify reserva exists if provided
            if (reservaPeriodoDto.getReservaDto() != null && reservaPeriodoDto.getReservaDto().getUuid() != null) {
                Reserva reserva = reservaRepository.findByUuid(reservaPeriodoDto.getReservaDto().getUuid())
                        .orElseThrow(() -> new NotFoundException(RESERVA, reservaPeriodoDto.getReservaDto().getUuid()));

                ReservaPeriodo nuevaReservaPeriodo = ReservaPeriodoMapper.toEntity(reservaPeriodoDto);
                nuevaReservaPeriodo.setReserva(reserva);

                return ReservaPeriodoMapper.toDto(reservaPeriodoRepository.save(nuevaReservaPeriodo),false);
            } else {
                throw new NullReferenceException(RESERVA); // Fixed: Use EntityType instead of String
            }
        } else {
            throw new NullReferenceException(RESERVA_PERIODO);
        }
    }

    @Override
    @Transactional
    public ReservaPeriodoDto actualizarReservaPeriodo(ReservaPeriodoDto reservaPeriodoDto) {
        if (reservaPeriodoDto != null) {
            Optional<ReservaPeriodo> reservaPeriodoOptional = reservaPeriodoRepository.findByUuid(reservaPeriodoDto.getUuid());

            if (reservaPeriodoOptional.isPresent()) {
                ReservaPeriodo reservaPeriodoExistente = reservaPeriodoOptional.get();
                ReservaPeriodoMapper.updateEntityFromDto(reservaPeriodoDto, reservaPeriodoExistente);

                return ReservaPeriodoMapper.toDto(reservaPeriodoRepository.save(reservaPeriodoExistente),false);
            } else {
                throw new NotFoundException(RESERVA_PERIODO, reservaPeriodoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(RESERVA_PERIODO);
        }
    }

    @Override
    @Transactional
    public String eliminarReservaPeriodo(String uuid) {
        ReservaPeriodo reservaPeriodo = reservaPeriodoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_PERIODO, uuid));
        reservaPeriodoRepository.delete(reservaPeriodo);
        return "Eliminación exitosa de la reserva de periodo con UUID: " + uuid;
    }
}