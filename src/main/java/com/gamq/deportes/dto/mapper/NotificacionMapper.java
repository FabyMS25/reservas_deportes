package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.model.Notificacion;

public class NotificacionMapper {

    public static NotificacionDto toDto(Notificacion notificacion) {
        NotificacionDto dto = new NotificacionDto();
        dto.setIdNotificacion(notificacion.getIdNotificacion());
        dto.setUuid(notificacion.getUuid());
        dto.setEstado(notificacion.isEstado());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setFechaActualizacion(notificacion.getFechaActualizacion());
        dto.setUsuarioUuid(notificacion.getUsuarioUuid());
        dto.setTipo(notificacion.getTipo());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setEstadoLectura(notificacion.getEstadoLectura());
        dto.setPrioridad(notificacion.getPrioridad());
        dto.setCanal(notificacion.getCanal());
        dto.setReferenciaTipo(notificacion.getReferenciaTipo());
        dto.setReferenciaUuid(notificacion.getReferenciaUuid());
        dto.setAccionUrl(notificacion.getAccionUrl());
        dto.setProgramadaPara(notificacion.getProgramadaPara());
        dto.setEnviadaEn(notificacion.getEnviadaEn());
        dto.setLeidaEn(notificacion.getLeidaEn());
        dto.setExpiraEn(notificacion.getExpiraEn());
        dto.setMetadataJson(notificacion.getMetadataJson());
        dto.setNotificacionAdminUuid(notificacion.getNotificacionAdminUuid());
        return dto;
    }

    public static Notificacion toEntity(NotificacionDto dto) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUuid(dto.getUuid());
        notificacion.setEstado(dto.isEstado());
        notificacion.setFechaCreacion(dto.getFechaCreacion());
        notificacion.setFechaActualizacion(dto.getFechaActualizacion());
        return updateEntityFromDto(dto, notificacion);
    }

    public static Notificacion updateEntityFromDto(NotificacionDto dto, Notificacion notificacion) {
        notificacion.setUsuarioUuid(dto.getUsuarioUuid());
        notificacion.setTipo(dto.getTipo());
        notificacion.setTitulo(dto.getTitulo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setEstadoLectura(dto.getEstadoLectura());
        notificacion.setPrioridad(dto.getPrioridad());
        notificacion.setCanal(dto.getCanal());
        notificacion.setReferenciaTipo(dto.getReferenciaTipo());
        notificacion.setReferenciaUuid(dto.getReferenciaUuid());
        notificacion.setAccionUrl(dto.getAccionUrl());
        notificacion.setProgramadaPara(dto.getProgramadaPara());
        notificacion.setEnviadaEn(dto.getEnviadaEn());
        notificacion.setLeidaEn(dto.getLeidaEn());
        notificacion.setExpiraEn(dto.getExpiraEn());
        notificacion.setMetadataJson(dto.getMetadataJson());
        notificacion.setNotificacionAdminUuid(dto.getNotificacionAdminUuid());
        return notificacion;
    }
}
