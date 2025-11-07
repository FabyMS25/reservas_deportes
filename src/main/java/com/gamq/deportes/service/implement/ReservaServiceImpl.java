package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.mapper.ReservaMapper;
import com.gamq.deportes.dto.mapper.ReservaPeriodoMapper;
import com.gamq.deportes.dto.mapper.ReservaPuntualMapper;
import com.gamq.deportes.dto.mapper.ReservaRecurrenteMapper;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.ReservaService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class ReservaServiceImpl implements ReservaService {

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
    private EstudianteEscuelaRepository estudianteEscuelaRepository;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaPuntualRepository reservaPuntualRepository;

    @Autowired
    private ReservaRecurrenteRepository reservaRecurrenteRepository;

    @Autowired
    private ReservaPeriodoRepository reservaPeriodoRepository;

    @Autowired
    private TableroRepository tableroRepository;

    @Autowired
    private ReservationNumberService reservationNumberService;

    @Autowired
    private ReservaPeriodoMapper reservaPeriodoMapper; // Add this

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
    public List<ReservaDto> obtenerReservas() {
        List<Reserva> reservas = reservaRepository.findAll();

        if (reservas.isEmpty()) {
            return List.of();
        }

        return reservas.stream()
                .map(ReservaMapper::toDto)
                .collect(Collectors.toList());
    }

    public Page<ReservaDto> obtenerReservas(Pageable pageable) {
        return reservaRepository.findAll(pageable)
                .map(ReservaMapper::toDto);
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
            Optional<Contribuyente> optional =  contribuyenteRepository.findByUuid(uuidContribuyente);
            if (optional.isPresent()) {
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

    private String generarNumeroReserva() {
        return reservationNumberService.findNextAvailableReservationNumber();
    }

    @Override
    @Transactional
    public ReservaDto crearReserva(ReservaDto reservaDto) {
        if (reservaDto == null) {
            throw new NullReferenceException(RESERVA);
        }

        CampoDeportivo campoDeportivo = getCampoDeportivo(reservaDto.getCampoDeportivoDto().getUuid());
        Contribuyente contribuyente = getContribuyente(reservaDto.getContribuyenteDto().getUuid());

        // Handle tarifa - can be null for periodo reservations
        Tarifa tarifa = null;
        if (reservaDto.getTarifaDto() != null && reservaDto.getTarifaDto().getUuid() != null) {
            tarifa = getTarifa(reservaDto.getTarifaDto().getUuid());
        }

        // Handle reservation number
        String numeroReserva = reservaDto.getNumeroReserva();
        boolean isUserProvided = false;

        if (numeroReserva != null && !numeroReserva.trim().isEmpty()) {
            if (!reservationNumberService.isReservationNumberAvailable(numeroReserva)) {
                throw new IllegalArgumentException("El número de reserva ya está en uso: " + numeroReserva);
            }
            isUserProvided = true;
        } else {
            numeroReserva = reservationNumberService.findNextAvailableReservationNumber();
            isUserProvided = false;
        }

        Reserva nuevaReserva = ReservaMapper.toEntity(reservaDto);
        nuevaReserva.setCampoDeportivo(campoDeportivo);
        nuevaReserva.setContribuyente(contribuyente);
        nuevaReserva.setTarifa(tarifa); // This can be null
        nuevaReserva.setNumeroReserva(numeroReserva);

        if (reservaDto.getOrganizacionDto() != null && reservaDto.getOrganizacionDto().getUuid() != null) {
            Organizacion organizacion = getOrganizacion(reservaDto.getOrganizacionDto().getUuid());
            nuevaReserva.setOrganizacion(organizacion);
        }

        // Handle estudiante escuela for periodo reservations
        if (reservaDto.getEstudianteEscuelaDto() != null && reservaDto.getEstudianteEscuelaDto().getUuid() != null) {
            EstudianteEscuela estudianteEscuela = getEstudianteEscuela(reservaDto.getEstudianteEscuelaDto().getUuid());
            nuevaReserva.setEstudianteEscuela(estudianteEscuela);
        }

        nuevaReserva.setFechaCreacion(LocalDateTime.now());
        nuevaReserva.setFechaActualizacion(LocalDateTime.now());

        if (nuevaReserva.getNumeroReserva() == null) {
            nuevaReserva.setNumeroReserva(generarNumeroReserva());
        }

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

        // Add periodo handling
        if ("periodo".equalsIgnoreCase(reservaDto.getTipoReserva()) && reservaDto.getReservaPeriodoDto() != null) {
            ReservaPeriodo periodo = reservaPeriodoMapper.toEntity(reservaDto.getReservaPeriodoDto());
            periodo.setReserva(nuevaReserva);
            nuevaReserva.setReservaPeriodo(periodo);
        }

        Reserva reservaGuardada = reservaRepository.save(nuevaReserva);
        return ReservaMapper.toDto(reservaGuardada);
    }

    @Override
    public ReservaDto actualizarReserva(ReservaDto reservaDto) {
        if (reservaDto == null) {
            throw new NullReferenceException(RESERVA);
        }

        Optional<Reserva> reservaOptional = reservaRepository.findByUuid(reservaDto.getUuid());

        if (reservaOptional.isPresent()) {
            Reserva reservaExistente = reservaOptional.get();

            ReservaMapper.updateEntityFromDto(reservaDto, reservaExistente);

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

            if (reservaDto.getEstudianteEscuelaDto() != null) {
                EstudianteEscuela estudianteEscuela = getEstudianteEscuela(reservaDto.getEstudianteEscuelaDto().getUuid());
                reservaExistente.setEstudianteEscuela(estudianteEscuela);
            }

            reservaExistente.setFechaActualizacion(LocalDateTime.now());

            Reserva reservaActualizada = reservaRepository.save(reservaExistente);
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

        reserva.setEstadoReserva("PENDIENTE");
        reserva.setEstadoValidacion("APROBADA");
        reserva.setAprobadoPor(aprobadoPor);
        reserva.setFechaAprobacion(LocalDateTime.now());
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaAprobada = reservaRepository.save(reserva);
        return ReservaMapper.toDto(reservaAprobada);
    }

    @Override
    @Transactional
    public ReservaDto rechazarReserva(String uuid, String motivoRechazo, String aprobadoPor) {
        Reserva reserva = reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));

        reserva.setEstadoValidacion("RECHAZADA");
        reserva.setMotivoRechazo(motivoRechazo);
        reserva.setAprobadoPor(aprobadoPor);
        reserva.setFechaRechazo(LocalDateTime.now());
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaRechazada = reservaRepository.save(reserva);
        return ReservaMapper.toDto(reservaRechazada);
    }

    @Override
    @Transactional
    public ReservaDto cancelarReserva(String uuid, String motivo) {
        Reserva reserva = reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));

        reserva.setEstadoReserva("CANCELADA");
        reserva.setMotivoRechazo(motivo);
        reserva.setFechaActualizacion(LocalDateTime.now());

        Reserva reservaCancelada = reservaRepository.save(reserva);
        return ReservaMapper.toDto(reservaCancelada);
    }

    @Override
    @Transactional
    public String eliminarReserva(String uuid) {
        try {
            Reserva reserva = reservaRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(RESERVA, uuid));

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

    private EstudianteEscuela getEstudianteEscuela(String uuid) {
        return estudianteEscuelaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(ESTUDIANTE_ESCUELA, uuid));
    }
}