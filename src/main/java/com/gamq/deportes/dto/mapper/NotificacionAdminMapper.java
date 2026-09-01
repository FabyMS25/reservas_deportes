package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.NotificacionAdminDto;
import com.gamq.deportes.model.NotificacionAdmin;

public final class NotificacionAdminMapper {

    private NotificacionAdminMapper() {
    }

    public static NotificacionAdminDto toDto(NotificacionAdmin entity) {
        return new NotificacionAdminDto()
                .setUuid(entity.getUuid())
                .setFechaCreacion(entity.getFechaCreacion())
                .setTipo(entity.getTipo())
                .setTitulo(entity.getTitulo())
                .setMensaje(entity.getMensaje())
                .setPrioridad(entity.getPrioridad())
                .setCanal(entity.getCanal())
                .setReferenciaTipo(entity.getReferenciaTipo())
                .setReferenciaUuid(entity.getReferenciaUuid())
                .setAccionUrl(entity.getAccionUrl())
                .setMetadataJson(entity.getMetadataJson())
                .setUsuarioExcluidoUuid(entity.getUsuarioExcluidoUuid());
    }

    public static NotificacionAdmin toEntity(NotificacionAdminDto dto) {
        return new NotificacionAdmin()
                .setUuid(dto.getUuid())
                .setFechaCreacion(dto.getFechaCreacion())
                .setTipo(dto.getTipo())
                .setTitulo(dto.getTitulo())
                .setMensaje(dto.getMensaje())
                .setPrioridad(dto.getPrioridad())
                .setCanal(dto.getCanal())
                .setReferenciaTipo(dto.getReferenciaTipo())
                .setReferenciaUuid(dto.getReferenciaUuid())
                .setAccionUrl(dto.getAccionUrl())
                .setMetadataJson(dto.getMetadataJson())
                .setUsuarioExcluidoUuid(dto.getUsuarioExcluidoUuid());
    }
}
