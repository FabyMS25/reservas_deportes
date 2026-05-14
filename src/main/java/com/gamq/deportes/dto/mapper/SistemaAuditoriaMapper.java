package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.SistemaAuditoriaDto;
import com.gamq.deportes.model.SistemaAuditoria;

public class SistemaAuditoriaMapper {

    public static SistemaAuditoriaDto toDto(SistemaAuditoria auditoria) {
        if (auditoria == null) return null;

        SistemaAuditoriaDto dto = new SistemaAuditoriaDto();
        dto.setIdSistemaAuditoria(auditoria.getIdSistemaAuditoria());
        dto.setUuid(auditoria.getUuid());
        dto.setEstado(auditoria.isEstado());
        dto.setFechaCreacion(auditoria.getFechaCreacion());
        dto.setFechaAccion(auditoria.getFechaAccion());
        dto.setUsuarioUuid(auditoria.getUsuarioUuid());
        dto.setAccion(auditoria.getAccion());
        dto.setModulo(auditoria.getModulo());
        dto.setEntidadTipo(auditoria.getEntidadTipo());
        dto.setEntidadUuid(auditoria.getEntidadUuid());
        dto.setDescripcion(auditoria.getDescripcion());
        dto.setDatosCambio(auditoria.getDatosCambio());
        dto.setDireccionIp(auditoria.getDireccionIp());
        dto.setUserAgent(auditoria.getUserAgent());

        return dto;
    }

    public static SistemaAuditoria toEntity(SistemaAuditoriaDto dto) {
        if (dto == null) return null;

        SistemaAuditoria auditoria = new SistemaAuditoria();
        auditoria.setUuid(dto.getUuid());
        auditoria.setEstado(dto.isEstado());
        auditoria.setFechaCreacion(dto.getFechaCreacion());
        auditoria.setFechaAccion(dto.getFechaAccion());

        return updateEntityFromDto(dto, auditoria);
    }

    public static SistemaAuditoria updateEntityFromDto(SistemaAuditoriaDto dto, SistemaAuditoria auditoria) {
        if (dto == null || auditoria == null) return auditoria;

        auditoria.setUsuarioUuid(dto.getUsuarioUuid());
        auditoria.setAccion(dto.getAccion());
        auditoria.setModulo(dto.getModulo());
        auditoria.setEntidadTipo(dto.getEntidadTipo());
        auditoria.setEntidadUuid(dto.getEntidadUuid());
        auditoria.setDescripcion(dto.getDescripcion());
        auditoria.setDatosCambio(dto.getDatosCambio());
        auditoria.setDireccionIp(dto.getDireccionIp());
        auditoria.setUserAgent(dto.getUserAgent());

        return auditoria;
    }
}
