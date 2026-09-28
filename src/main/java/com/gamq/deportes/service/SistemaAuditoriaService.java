package com.gamq.deportes.service;

import com.gamq.deportes.dto.SistemaAuditoriaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface SistemaAuditoriaService {
    SistemaAuditoriaDto findByUuid(String uuid);
    List<SistemaAuditoriaDto> obtenerSistemaAuditorias();
    Page<SistemaAuditoriaDto> obtenerSistemaAuditorias(Pageable pageable);
    List<SistemaAuditoriaDto> findByUsuarioUuid(String usuarioUuid);
    List<SistemaAuditoriaDto> findByAccion(String accion);
    List<SistemaAuditoriaDto> findByModulo(String modulo);
    List<SistemaAuditoriaDto> findByEntidad(String entidadTipo, String entidadUuid);
    List<SistemaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    Page<SistemaAuditoriaDto> findByFechaAccionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    SistemaAuditoriaDto crearSistemaAuditoria(SistemaAuditoriaDto auditoriaDto);
    void registrarAccion(String usuarioUuid, String accion, String modulo, String entidadTipo, String entidadUuid,
                         String descripcion, String datosCambio, String direccionIp, String userAgent);
    String delete(String uuid);
    String snapshotPago(com.gamq.deportes.model.Pago pago);
    void registrarPago(String accion, String anterior, com.gamq.deportes.model.Pago pago);
    List<Object[]> getEstadisticasPorAccion();
    List<Object[]> getEstadisticasPorModulo();
    List<Object[]> getEstadisticasPorUsuario();
}
