package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaAuditoriaDto;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaAuditoria;

public class ReservaAuditoriaMapper {

    public static ReservaAuditoriaDto toDto(ReservaAuditoria auditoria, boolean includeRelations) {
        if (auditoria == null) return null;

        ReservaAuditoriaDto dto = new ReservaAuditoriaDto();
        dto.setUuid(auditoria.getUuid());

        dto.setAccion(auditoria.getAccion());
        dto.setEstadoAnterior(auditoria.getEstadoAnterior());
        dto.setEstadoNuevo(auditoria.getEstadoNuevo());
        dto.setUsuarioUuid(auditoria.getUsuarioUuid());
        dto.setDatosCambio(auditoria.getDatosCambio());
        dto.setObservaciones(auditoria.getObservaciones());
        dto.setDireccionIp(auditoria.getDireccionIp());
        dto.setFechaAccion(auditoria.getFechaAccion());

        if (includeRelations && auditoria.getReserva() != null) {
            dto.setReservaDto(ReservaMapper.toDto(auditoria.getReserva()));
        }

        return dto;
    }

    public static ReservaAuditoria toEntity(ReservaAuditoriaDto dto) {
        if (dto == null) return null;

        ReservaAuditoria auditoria = new ReservaAuditoria();
        auditoria.setUuid(dto.getUuid());

        auditoria.setAccion(dto.getAccion());
        auditoria.setEstadoAnterior(dto.getEstadoAnterior());
        auditoria.setEstadoNuevo(dto.getEstadoNuevo());
        auditoria.setUsuarioUuid(dto.getUsuarioUuid());
        auditoria.setDatosCambio(dto.getDatosCambio());
        auditoria.setObservaciones(dto.getObservaciones());
        auditoria.setDireccionIp(dto.getDireccionIp());
        auditoria.setFechaAccion(dto.getFechaAccion());

        return auditoria;
    }

    // Helper method to create audit entry from reservation changes
    public static ReservaAuditoria createAuditEntry(Reserva reserva, String accion, String usuarioUuid,
                                                    String estadoAnterior, String estadoNuevo,
                                                    String datosCambio, String observaciones, String direccionIp) {
        ReservaAuditoria auditoria = new ReservaAuditoria();
        auditoria.setReserva(reserva);
        auditoria.setAccion(accion);
        auditoria.setUsuarioUuid(usuarioUuid);
        auditoria.setEstadoAnterior(estadoAnterior);
        auditoria.setEstadoNuevo(estadoNuevo);
        auditoria.setDatosCambio(datosCambio);
        auditoria.setObservaciones(observaciones);
        auditoria.setDireccionIp(direccionIp);

        return auditoria;
    }
}