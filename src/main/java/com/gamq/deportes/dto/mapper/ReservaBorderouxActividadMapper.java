package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaBorderouxActividadDto;
import com.gamq.deportes.model.ReservaBorderouxActividad;

import java.util.stream.Collectors;

public class ReservaBorderouxActividadMapper {

    private ReservaBorderouxActividadMapper() {
    }

    public static ReservaBorderouxActividadDto toDto(
            ReservaBorderouxActividad actividad, boolean includeReservation) {
        if (actividad == null) {
            return null;
        }
        ReservaBorderouxActividadDto dto = new ReservaBorderouxActividadDto();
        dto.setIdReservaBorderouxActividad(actividad.getIdReservaBorderouxActividad());
        dto.setUuid(actividad.getUuid());
        dto.setEstado(actividad.isEstado());
        dto.setFechaCreacion(actividad.getFechaCreacion());
        dto.setFechaActividad(actividad.getFechaActividad());
        dto.setPorcentajeAplicado(actividad.getPorcentajeAplicado());
        dto.setSubtotal(actividad.getSubtotal());
        dto.setObservaciones(actividad.getObservaciones());

        if (actividad.getCampoDeportivo() != null) {
            dto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(actividad.getCampoDeportivo(), false));
        }
        if (actividad.getCategoriaDeportiva() != null) {
            dto.setCategoriaDeportivaDto(CategoriaMapper.toDto(actividad.getCategoriaDeportiva()));
        }
        if (actividad.getTalonariosList() != null) {
            dto.setTalonariosDtoList(actividad.getTalonariosList().stream()
                    .map(ReservaBorderouxTalonarioMapper::toDto)
                    .collect(Collectors.toList()));
        }
        if (includeReservation && actividad.getReserva() != null) {
            dto.setReservaDto(ReservaMapper.toDto(actividad.getReserva()));
        }
        return dto;
    }

    public static ReservaBorderouxActividad toEntity(ReservaBorderouxActividadDto dto) {
        if (dto == null) {
            return null;
        }
        ReservaBorderouxActividad actividad = new ReservaBorderouxActividad();
        actividad.setUuid(dto.getUuid());
        actividad.setEstado(dto.isEstado());
        actividad.setFechaActividad(dto.getFechaActividad());
        actividad.setPorcentajeAplicado(dto.getPorcentajeAplicado());
        actividad.setSubtotal(dto.getSubtotal());
        actividad.setObservaciones(dto.getObservaciones());
        return actividad;
    }
}
