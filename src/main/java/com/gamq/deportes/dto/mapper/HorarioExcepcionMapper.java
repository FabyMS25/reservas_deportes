package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.HorarioExcepcionDto;
import com.gamq.deportes.model.HorarioExcepcion;

public class HorarioExcepcionMapper {

    public static HorarioExcepcionDto toDto(HorarioExcepcion excepcion, boolean includeCampo) {
        HorarioExcepcionDto dto = new HorarioExcepcionDto();

        dto.setUuid(excepcion.getUuid());
        dto.setEstado(excepcion.isEstado());
        dto.setFechaCreacion(excepcion.getFechaCreacion());

        dto.setFecha(excepcion.getFecha());
        dto.setTipoExcepcion(excepcion.getTipoExcepcion());
        dto.setHoraApertura(excepcion.getHoraApertura());
        dto.setHoraCierre(excepcion.getHoraCierre());
        dto.setMotivo(excepcion.getMotivo());

        if (includeCampo) {
            dto.setCampoDeportivoDto(CampoDeportivoMapper.toSimpleDto(excepcion.getCampoDeportivo()));

        }
        return dto;
    }

    public static HorarioExcepcion toEntity(HorarioExcepcionDto dto) {
        HorarioExcepcion excepcion = new HorarioExcepcion();
        return updateEntityFromDto(dto, excepcion);
    }

    public static HorarioExcepcion updateEntityFromDto(HorarioExcepcionDto dto, HorarioExcepcion excepcion) {
//        excepcion.setUuid(dto.getUuid());
//        excepcion.setEstado(dto.isEstado());
        excepcion.setFecha(dto.getFecha());
        excepcion.setTipoExcepcion(dto.getTipoExcepcion());
        excepcion.setHoraApertura(dto.getHoraApertura());
        excepcion.setHoraCierre(dto.getHoraCierre());
        excepcion.setMotivo(dto.getMotivo());

        return excepcion;
    }
}
