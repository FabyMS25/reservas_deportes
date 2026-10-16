package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ReservaAuditoriaDto;
import com.gamq.deportes.dto.mapper.ReservaAuditoriaMapper;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaAuditoria;
import com.gamq.deportes.repository.ReservaAuditoriaRepository;
import com.gamq.deportes.repository.ReservaRepository;
import com.gamq.deportes.service.ReservaAuditoriaService;
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

@Service
@Transactional
public class ReservaAuditoriaServiceImpl implements ReservaAuditoriaService {

    @Autowired
    private ReservaAuditoriaRepository reservaAuditoriaRepository;

    @Autowired
    private ReservaRepository reservaRepository;


    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> obtenerTodasAuditorias() {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findAll();

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaAuditoriaDto> obtenerTodasAuditoriasPage(Pageable pageable) {
        return reservaAuditoriaRepository.findAll(pageable)
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservaAuditoriaDto findByUuid(String uuid) {
        Optional<ReservaAuditoria> auditoriaOptional = reservaAuditoriaRepository.findByUuid(uuid);
        if (auditoriaOptional.isPresent()) {
            return ReservaAuditoriaMapper.toDto(auditoriaOptional.get(), true);
        } else {
            throw new NotFoundException(RESERVA_AUDITORIA, uuid);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByReservaUuid(String reservaUuid) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByReservaUuid(reservaUuid);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaAuditoriaDto> findByReservaUuid(String reservaUuid, Pageable pageable) {
        return reservaAuditoriaRepository.findByReservaUuid(reservaUuid, pageable)
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByAccion(String accion) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByAccion(accion);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByAccionIn(List<String> acciones) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByAccionIn(acciones);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByUsuarioUuid(String usuarioUuid) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByUsuarioUuid(usuarioUuid);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByFechaAccionBetween(fechaInicio, fechaFin);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReservaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return reservaAuditoriaRepository.findByFechaAccionBetween(fechaInicio, fechaFin, pageable)
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findByReservaUuidAndAccion(String reservaUuid, String accion) {
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findByReservaUuidAndAccion(reservaUuid, accion);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservaAuditoriaDto> findLatestByReservaUuid(String reservaUuid, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        List<ReservaAuditoria> auditorias = reservaAuditoriaRepository.findLatestByReservaUuid(reservaUuid, pageable);

        return auditorias.stream()
                .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ReservaAuditoriaDto crearAuditoria(ReservaAuditoriaDto auditoriaDto) {
        if (auditoriaDto == null) {
            throw new NullReferenceException(RESERVA_AUDITORIA);
        }

        // Validate and get reservation
        Reserva reserva = getReserva(auditoriaDto.getReservaDto().getUuid());

        ReservaAuditoria nuevaAuditoria = ReservaAuditoriaMapper.toEntity(auditoriaDto);
        nuevaAuditoria.setReserva(reserva);

        // Set default values
        if (nuevaAuditoria.getFechaAccion() == null) {
            nuevaAuditoria.setFechaAccion(LocalDateTime.now());
        }

        ReservaAuditoria auditoriaGuardada = reservaAuditoriaRepository.save(nuevaAuditoria);
        return ReservaAuditoriaMapper.toDto(auditoriaGuardada, true);
    }

    @Override
    @Transactional
    public void registrarAccion(String reservaUuid, String accion, String usuarioUuid,
                                String estadoAnterior, String estadoNuevo,
                                String datosCambio, String observaciones, String direccionIp) {
        Reserva reserva = getReserva(reservaUuid);

        ReservaAuditoria auditoria = new ReservaAuditoria();
        auditoria.setReserva(reserva);
        auditoria.setAccion(accion);
        auditoria.setUsuarioUuid(usuarioUuid);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setDatosCambio(datosCambio);
        auditoria.setObservaciones(observaciones);
        auditoria.setDireccionIp(direccionIp);
        auditoria.setFechaAccion(LocalDateTime.now());

        reservaAuditoriaRepository.save(auditoria);
    }

//    @Transactional(readOnly = true)
//    public Long countByReservaUuid(String reservaUuid) {
//        return reservaAuditoriaRepository.countByReservaUuid(reservaUuid);
//    }
//
//    @Transactional(readOnly = true)
//    public Long countByAccion(String accion) {
//        return reservaAuditoriaRepository.countByAccion(accion);
//    }
//
//    @Transactional(readOnly = true)
//    public Long countByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
//        return reservaAuditoriaRepository.countByFechaAccionBetween(fechaInicio, fechaFin);
//    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorAccion() {
        return reservaAuditoriaRepository.countByAccionGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorUsuario() {
        return reservaAuditoriaRepository.countByUsuarioUuidGroup();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getEstadisticasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return reservaAuditoriaRepository.countByFechaAccionGroup(fechaInicio, fechaFin);
    }

    @Override
    @Transactional
    public String eliminarAuditoria(String uuid) {
        try {
        ReservaAuditoria auditoria = reservaAuditoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA_AUDITORIA, uuid));
        reservaAuditoriaRepository.delete(auditoria);
            return "Desactivación exitosa del RESERVA_AUDITORIA";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el RESERVA_AUDITORIA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }
    private Reserva getReserva(String uuid) {
        return reservaRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(RESERVA, uuid));
    }
}