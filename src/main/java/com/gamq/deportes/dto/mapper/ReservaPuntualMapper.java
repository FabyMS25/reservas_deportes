package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.model.ReservaPuntual;

import java.time.LocalDateTime;

public class ReservaPuntualMapper {

    public static ReservaPuntualDto toDto(ReservaPuntual reservaPuntual, boolean includeReserva) {
        if (reservaPuntual == null) {
            return null;
        }

        ReservaPuntualDto dto = new ReservaPuntualDto();
        dto.setUuid(reservaPuntual.getUuid());
        dto.setEstado(reservaPuntual.isEstado());

        // Convert LocalDateTime to Date for fechaCreacion in DTO
        if (reservaPuntual.getFechaCreacion() != null) {
            dto.setFechaCreacion(java.sql.Timestamp.valueOf(reservaPuntual.getFechaCreacion()));
        }

        dto.setFechaUso(reservaPuntual.getFechaUso());
        dto.setFechaUsoReal(reservaPuntual.getFechaUsoReal());
        dto.setHoraInicio(reservaPuntual.getHoraInicio());
        dto.setHoraFin(reservaPuntual.getHoraFin());
        dto.setDuracionHoras(reservaPuntual.getDuracionHoras());
        dto.setCantidadPersonas(reservaPuntual.getCantidadPersonas());
        dto.setPrecioBaseAplicado(reservaPuntual.getPrecioBaseAplicado());
        dto.setCategoria(reservaPuntual.getCategoria());
        dto.setSuperficie(reservaPuntual.getSuperficie());
        dto.setPeriodo(reservaPuntual.getPeriodo());
        dto.setObservaciones(reservaPuntual.getObservaciones());

        // Map parent reservation if requested and available
        if (includeReserva && reservaPuntual.getReserva() != null) {
            dto.setReservaDto(ReservaMapper.toDto(reservaPuntual.getReserva()));
        }

        return dto;
    }

    public static ReservaPuntual toEntity(ReservaPuntualDto reservaPuntualDto, boolean includeReserva) {
        if (reservaPuntualDto == null) {
            return null;
        }

        ReservaPuntual entity = new ReservaPuntual();
        entity.setUuid(reservaPuntualDto.getUuid());
        entity.setEstado(reservaPuntualDto.isEstado());

        // Convert Date to LocalDateTime for fechaCreacion in entity
        if (reservaPuntualDto.getFechaCreacion() != null) {
            if (reservaPuntualDto.getFechaCreacion() instanceof java.sql.Timestamp) {
                entity.setFechaCreacion(((java.sql.Timestamp) reservaPuntualDto.getFechaCreacion()).toLocalDateTime());
            } else {
                // Fallback for other Date types
                entity.setFechaCreacion(LocalDateTime.ofInstant(
                        reservaPuntualDto.getFechaCreacion().toInstant(),
                        java.time.ZoneId.systemDefault()
                ));
            }
        }

        entity.setFechaUso(reservaPuntualDto.getFechaUso());
        entity.setFechaUsoReal(reservaPuntualDto.getFechaUsoReal());
        entity.setHoraInicio(reservaPuntualDto.getHoraInicio());
        entity.setHoraFin(reservaPuntualDto.getHoraFin());
        entity.setDuracionHoras(reservaPuntualDto.getDuracionHoras());
        entity.setCantidadPersonas(reservaPuntualDto.getCantidadPersonas());
        entity.setPrecioBaseAplicado(reservaPuntualDto.getPrecioBaseAplicado());
        entity.setCategoria(reservaPuntualDto.getCategoria());
        entity.setSuperficie(reservaPuntualDto.getSuperficie());
        entity.setPeriodo(reservaPuntualDto.getPeriodo());
        entity.setObservaciones(reservaPuntualDto.getObservaciones());

        if (includeReserva && reservaPuntualDto.getReservaDto() != null) {
            entity.setReserva(ReservaMapper.toEntity(reservaPuntualDto.getReservaDto()));
        }

        return entity;
    }

    public static ReservaPuntual updateEntityFromDto(ReservaPuntualDto dto, ReservaPuntual entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        // Only update fields that should be updated (typically not ID, UUID, creation date)
        entity.setFechaUso(dto.getFechaUso());
        entity.setFechaUsoReal(dto.getFechaUsoReal());
        entity.setHoraInicio(dto.getHoraInicio());
        entity.setHoraFin(dto.getHoraFin());
        entity.setDuracionHoras(dto.getDuracionHoras());
        entity.setCantidadPersonas(dto.getCantidadPersonas());
        entity.setPrecioBaseAplicado(dto.getPrecioBaseAplicado());
        entity.setCategoria(dto.getCategoria());
        entity.setSuperficie(dto.getSuperficie());
        entity.setPeriodo(dto.getPeriodo());
        entity.setObservaciones(dto.getObservaciones());

        return entity;
    }

    // Simple mapping without relationships
    public static ReservaPuntualDto toSimpleDto(ReservaPuntual reservaPuntual) {
        return toDto(reservaPuntual, false);
    }

    public static ReservaPuntual toSimpleEntity(ReservaPuntualDto reservaPuntualDto) {
        return toEntity(reservaPuntualDto, false);
    }
}
