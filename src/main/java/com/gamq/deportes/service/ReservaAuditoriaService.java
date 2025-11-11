package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservaAuditoriaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaAuditoriaService {

    ReservaAuditoriaDto findByUuid(String uuid);
    List<ReservaAuditoriaDto> obtenerTodasAuditorias();
    Page<ReservaAuditoriaDto> obtenerTodasAuditoriasPage(Pageable pageable);
    List<ReservaAuditoriaDto> findByReservaUuid(String reservaUuid);
    Page<ReservaAuditoriaDto> findByReservaUuid(String reservaUuid, Pageable pageable);
    List<ReservaAuditoriaDto> findByAccion(String accion);
    List<ReservaAuditoriaDto> findByAccionIn(List<String> acciones);
    List<ReservaAuditoriaDto> findByUsuarioUuid(String usuarioUuid);
    List<ReservaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    Page<ReservaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);

    List<ReservaAuditoriaDto> findByReservaUuidAndAccion(String reservaUuid, String accion);

    List<ReservaAuditoriaDto> findLatestByReservaUuid(String reservaUuid, int limit);

    ReservaAuditoriaDto crearAuditoria(ReservaAuditoriaDto auditoriaDto);

    void registrarAccion(String reservaUuid, String accion, String usuarioUuid,
                         String estadoAnterior, String estadoNuevo,
                         String datosCambio, String observaciones, String direccionIp);

    String eliminarAuditoria(String uuid);
//    Long countByReservaUuid(String reservaUuid);
//    Long countByAccion(String accion);

//    Long countByUsuarioUuid(String usuarioUuid);

//    Long countByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Object[]> getEstadisticasPorAccion();
    List<Object[]> getEstadisticasPorUsuario();

    List<Object[]> getEstadisticasPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}