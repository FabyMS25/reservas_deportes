package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.dto.mapper.ReservaMapper;
import com.gamq.deportes.dto.mapper.ReservaPuntualMapper;
import com.gamq.deportes.dto.mapper.ReservaRecurrenteMapper;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaRecurrente;
import com.gamq.deportes.model.Tablero;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.repository.TableroRepository;
import com.gamq.deportes.service.ReservaRecurrenteService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class ReservaRecurrenteServiceImpl implements ReservaRecurrenteService {

    @Autowired
    private ReservaRecurrenteRepository reservaRecurrenteRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private TableroRepository tableroRepository;

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
    public List<ReservaRecurrenteDto> findActivasPorDiaSemana(Integer diaSemana, LocalDate fecha) {
        // First get all active reservations for the date
        List<ReservaRecurrente> reservasActivas = reservaRecurrenteRepository.findActivasEnFecha(fecha);

        // Then filter by day of week using Java
        return reservasActivas.stream()
                .filter(reserva -> {
                    Integer diasSemana = reserva.getDiasSemana();
                    if (diasSemana == null) return false;

                    // Simple bitmask check in Java
                    return (diasSemana & diaSemana) != 0;
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

        // Set tablero if provided
        if (reservaRecurrenteDto.getTableroDto() != null && reservaRecurrenteDto.getTableroDto().getUuid() != null) {
            Tablero tablero = getTablero(reservaRecurrenteDto.getTableroDto().getUuid());
            nuevaReservaRecurrente.setTablero(tablero);
        }

        // Set default values
        if (nuevaReservaRecurrente.getActivo() == null) {
            nuevaReservaRecurrente.setActivo(true);
        }

        // Validate recurrence dates
        if (nuevaReservaRecurrente.getFechaFin() != null &&
                nuevaReservaRecurrente.getFechaInicio().isAfter(nuevaReservaRecurrente.getFechaFin())) {
            throw new IllegalArgumentException("Fecha de inicio no puede ser después de la fecha de fin");
        }

        ReservaRecurrente reservaRecurrenteGuardada = reservaRecurrenteRepository.save(nuevaReservaRecurrente);
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteGuardada, true);
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

            // Update fields
            ReservaRecurrenteMapper.updateEntityFromDto(reservaRecurrenteDto, reservaRecurrenteExistente);

            // Update parent reservation if provided
            if (reservaRecurrenteDto.getReservaDto() != null) {
                Reserva reserva = getReserva(reservaRecurrenteDto.getReservaDto().getUuid());
                reservaRecurrenteExistente.setReserva(reserva);
            }

            // Update tablero if provided
            if (reservaRecurrenteDto.getTableroDto() != null) {
                Tablero tablero = getTablero(reservaRecurrenteDto.getTableroDto().getUuid());
                reservaRecurrenteExistente.setTablero(tablero);
            } else {
                reservaRecurrenteExistente.setTablero(null);
            }

            // Validate recurrence dates
            if (reservaRecurrenteDto.getFechaFin() != null &&
                    reservaRecurrenteDto.getFechaInicio().isAfter(reservaRecurrenteDto.getFechaFin())) {
                throw new IllegalArgumentException("Fecha de inicio no puede ser después de la fecha de fin");
            }

            ReservaRecurrente reservaRecurrenteActualizada = reservaRecurrenteRepository.save(reservaRecurrenteExistente);
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
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteActualizada, true);
    }

    @Override
    @Transactional
    public ReservaRecurrenteDto desactivarReservaRecurrente(String uuid) {
        ReservaRecurrente reservaRecurrente = reservaRecurrenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_RECURRENTE, uuid));

        reservaRecurrente.setActivo(false);

        ReservaRecurrente reservaRecurrenteActualizada = reservaRecurrenteRepository.save(reservaRecurrente);
        return ReservaRecurrenteMapper.toDto(reservaRecurrenteActualizada, true);
    }

    @Override
    @Transactional
    public String eliminarReserva(String uuid) {
        try {
        ReservaRecurrente reserva = reservaRecurrenteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_RECURRENTE, uuid));
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
}