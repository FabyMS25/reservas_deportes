package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.dto.TableroDto;
import com.gamq.deportes.model.ReservaRecurrente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReservaRecurrenteMapper {

    public static ReservaRecurrenteDto toDto(ReservaRecurrente reservaRecurrente, boolean includeReserva) {
        if (reservaRecurrente == null) {
            return null;
        }

        ReservaRecurrenteDto dto = new ReservaRecurrenteDto();
        dto.setUuid(reservaRecurrente.getUuid());
        dto.setEstado(reservaRecurrente.isEstado());

        // Convert LocalDateTime to Date for fechaCreacion in DTO
        if (reservaRecurrente.getFechaCreacion() != null) {
            dto.setFechaCreacion(java.sql.Timestamp.valueOf(reservaRecurrente.getFechaCreacion()));
        }

        dto.setTipoRecurrencia(reservaRecurrente.getTipoRecurrencia());
        dto.setFechaInicio(reservaRecurrente.getFechaInicio());
        dto.setFechaFin(reservaRecurrente.getFechaFin());
        dto.setCantidadPersonas(reservaRecurrente.getCantidadPersonas());

        // Convert Integer to String for diasSemana in DTO
        if (reservaRecurrente.getDiasSemana() != null) {
            dto.setDiasSemana(String.valueOf(reservaRecurrente.getDiasSemana()));
        }

        dto.setCantidadSesiones(reservaRecurrente.getCantidadSesiones());
        dto.setDuracionSesionHoras(reservaRecurrente.getDuracionSesionHoras());

        // Convert Integer to BigDecimal for tarifaMensual in DTO
        if (reservaRecurrente.getTarifaMensual() != null) {
            dto.setTarifaMensual(BigDecimal.valueOf(reservaRecurrente.getTarifaMensual()));
        }

        dto.setActivo(reservaRecurrente.getActivo());
        dto.setCategoria(reservaRecurrente.getCategoria());
        dto.setSuperficie(reservaRecurrente.getSuperficie());
        dto.setObservaciones(reservaRecurrente.getObservaciones());

        // Map tablero if available
        if (reservaRecurrente.getTablero() != null) {
            dto.setTableroDto(TableroMapper.toDto(reservaRecurrente.getTablero()));
        }

        // Map parent reservation if requested and available
        if (includeReserva && reservaRecurrente.getReserva() != null) {
            dto.setReservaDto(ReservaMapper.toDto(reservaRecurrente.getReserva()));
        }

        return dto;
    }

    public static ReservaRecurrente toEntity(ReservaRecurrenteDto reservaRecurrenteDto, boolean includeReserva) {
        if (reservaRecurrenteDto == null) {
            return null;
        }

        ReservaRecurrente entity = new ReservaRecurrente();
        entity.setUuid(reservaRecurrenteDto.getUuid());
        entity.setEstado(reservaRecurrenteDto.isEstado());

        // Convert Date to LocalDateTime for fechaCreacion in entity
        if (reservaRecurrenteDto.getFechaCreacion() != null) {
            if (reservaRecurrenteDto.getFechaCreacion() instanceof java.sql.Timestamp) {
                entity.setFechaCreacion(((java.sql.Timestamp) reservaRecurrenteDto.getFechaCreacion()).toLocalDateTime());
            } else {
                // Fallback for other Date types
                entity.setFechaCreacion(LocalDateTime.ofInstant(
                        reservaRecurrenteDto.getFechaCreacion().toInstant(),
                        java.time.ZoneId.systemDefault()
                ));
            }
        }

        entity.setTipoRecurrencia(reservaRecurrenteDto.getTipoRecurrencia());
        entity.setFechaInicio(reservaRecurrenteDto.getFechaInicio());
        entity.setFechaFin(reservaRecurrenteDto.getFechaFin());
        entity.setCantidadPersonas(reservaRecurrenteDto.getCantidadPersonas());

        // Convert String to Integer for diasSemana in entity
        if (reservaRecurrenteDto.getDiasSemana() != null && !reservaRecurrenteDto.getDiasSemana().isEmpty()) {
            try {
                entity.setDiasSemana(Integer.parseInt(reservaRecurrenteDto.getDiasSemana()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid diasSemana format: " + reservaRecurrenteDto.getDiasSemana());
            }
        }

        entity.setCantidadSesiones(reservaRecurrenteDto.getCantidadSesiones());
        entity.setDuracionSesionHoras(reservaRecurrenteDto.getDuracionSesionHoras());

        // Convert BigDecimal to Integer for tarifaMensual in entity
        if (reservaRecurrenteDto.getTarifaMensual() != null) {
            entity.setTarifaMensual(reservaRecurrenteDto.getTarifaMensual().intValue());
        }

        entity.setActivo(reservaRecurrenteDto.getActivo());
        entity.setCategoria(reservaRecurrenteDto.getCategoria());
        entity.setSuperficie(reservaRecurrenteDto.getSuperficie());
        entity.setObservaciones(reservaRecurrenteDto.getObservaciones());

        // Map tablero if available
        if (reservaRecurrenteDto.getTableroDto() != null) {
            entity.setTablero(TableroMapper.toEntity(reservaRecurrenteDto.getTableroDto()));
        }

        // Map parent reservation if requested and available
        if (includeReserva && reservaRecurrenteDto.getReservaDto() != null) {
            entity.setReserva(ReservaMapper.toEntity(reservaRecurrenteDto.getReservaDto()));
        }

        return entity;
    }

    // Method to update an existing entity from DTO (for partial updates)
    public static ReservaRecurrente updateEntityFromDto(ReservaRecurrenteDto dto, ReservaRecurrente entity) {
        if (dto == null || entity == null) {
            return entity;
        }

        // Only update fields that should be updated (typically not ID, UUID, creation date)
        entity.setTipoRecurrencia(dto.getTipoRecurrencia());
        entity.setFechaInicio(dto.getFechaInicio());
        entity.setFechaFin(dto.getFechaFin());
        entity.setCantidadPersonas(dto.getCantidadPersonas());

        // Convert String to Integer for diasSemana
        if (dto.getDiasSemana() != null && !dto.getDiasSemana().isEmpty()) {
            try {
                entity.setDiasSemana(Integer.parseInt(dto.getDiasSemana()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid diasSemana format: " + dto.getDiasSemana());
            }
        } else {
            entity.setDiasSemana(null);
        }

        entity.setCantidadSesiones(dto.getCantidadSesiones());
        entity.setDuracionSesionHoras(dto.getDuracionSesionHoras());

        // Convert BigDecimal to Integer for tarifaMensual
        if (dto.getTarifaMensual() != null) {
            entity.setTarifaMensual(dto.getTarifaMensual().intValue());
        } else {
            entity.setTarifaMensual(null);
        }

        entity.setActivo(dto.getActivo());
        entity.setCategoria(dto.getCategoria());
        entity.setSuperficie(dto.getSuperficie());
        entity.setObservaciones(dto.getObservaciones());

        return entity;
    }

    // Simple mapping without relationships
    public static ReservaRecurrenteDto toSimpleDto(ReservaRecurrente reservaRecurrente) {
        return toDto(reservaRecurrente, false);
    }

    public static ReservaRecurrente toSimpleEntity(ReservaRecurrenteDto reservaRecurrenteDto) {
        return toEntity(reservaRecurrenteDto, false);
    }
}