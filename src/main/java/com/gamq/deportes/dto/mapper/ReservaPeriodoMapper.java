package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.model.ReservaPeriodo;
import org.springframework.stereotype.Component;

@Component
public class ReservaPeriodoMapper {

    public static ReservaPeriodoDto toDto(ReservaPeriodo reservaPeriodo, boolean includeDetails) {
        if (reservaPeriodo == null) return null;

        ReservaPeriodoDto dto = new ReservaPeriodoDto();

        dto.setUuid(reservaPeriodo.getUuid());
        dto.setEstado(reservaPeriodo.isEstado());
        dto.setFechaCreacion(java.sql.Timestamp.valueOf(reservaPeriodo.getFechaCreacion()));

        // Period information
        dto.setMes(reservaPeriodo.getMes());
        dto.setAño(reservaPeriodo.getAño());

        // School context
        dto.setMatricula(reservaPeriodo.getMatricula());
        dto.setDisciplina(reservaPeriodo.getDisciplina());
        dto.setEntrenador(reservaPeriodo.getEntrenador());

        if (reservaPeriodo.getEstudianteEscuela() != null) {
            dto.setEstudianteEscuelaDto(
                    EstudianteEscuelaMapper.toDto(reservaPeriodo.getEstudianteEscuela(), false));
        }
        if (includeDetails) {
            dto.setReservaDto(ReservaMapper.toDto(reservaPeriodo.getReserva()));
        }
        return dto;
    }

    public static ReservaPeriodo toEntity(ReservaPeriodoDto dto) {
        if (dto == null) return null;

        ReservaPeriodo reservaPeriodo = new ReservaPeriodo();

        reservaPeriodo.setUuid(dto.getUuid());
        reservaPeriodo.setEstado(dto.isEstado());
        if (dto.getFechaCreacion() != null) {
            reservaPeriodo.setFechaCreacion(dto.getFechaCreacion().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime());
        } else {
            reservaPeriodo.setFechaCreacion(java.time.LocalDateTime.now());
        }

        // Period information
        reservaPeriodo.setMes(dto.getMes());
        reservaPeriodo.setAño(dto.getAño());

        // School context
        reservaPeriodo.setMatricula(dto.getMatricula());
        reservaPeriodo.setDisciplina(dto.getDisciplina());
        reservaPeriodo.setEntrenador(dto.getEntrenador());

        return reservaPeriodo;
    }

    public static ReservaPeriodo updateEntityFromDto(ReservaPeriodoDto dto, ReservaPeriodo reservaPeriodo) {
        if (dto == null || reservaPeriodo == null) return reservaPeriodo;

        // Period information
        reservaPeriodo.setMes(dto.getMes());
        reservaPeriodo.setAño(dto.getAño());

        // School context
        reservaPeriodo.setMatricula(dto.getMatricula());
        reservaPeriodo.setDisciplina(dto.getDisciplina());
        reservaPeriodo.setEntrenador(dto.getEntrenador());

        return reservaPeriodo;
    }
}
