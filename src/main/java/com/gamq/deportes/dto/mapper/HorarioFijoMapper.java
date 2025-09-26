package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.HorarioFijoDto;
import com.gamq.deportes.model.HorarioFijo;

public class HorarioFijoMapper {

    public static HorarioFijoDto toDto(HorarioFijo horario, boolean includeCampo) {
        HorarioFijoDto dto = new HorarioFijoDto();

        dto.setUuid(horario.getUuid());
        dto.setEstado(horario.isEstado());
        dto.setFechaCreacion(horario.getFechaCreacion());

        dto.setDia(horario.getDia());
        dto.setHoraApertura(horario.getHoraApertura());
        dto.setHoraCierre(horario.getHoraCierre());
        dto.setAbierto(horario.isAbierto());
        dto.setObservaciones(horario.getObservaciones());

        if (includeCampo) {
            dto.setCampoDeportivoDto(CampoDeportivoMapper.toSimpleDto(horario.getCampoDeportivo()));
        }

        return dto;
    }
    public static HorarioFijo toEntity(HorarioFijoDto dto) {
        HorarioFijo horario = new HorarioFijo();
        return updateEntityFromDto(dto, horario);
    }

    public static HorarioFijo updateEntityFromDto(HorarioFijoDto dto, HorarioFijo horario) {
//        horario.setUuid(dto.getUuid());
//        horario.setEstado(dto.isEstado());
        horario.setDia(dto.getDia());
        horario.setHoraApertura(dto.getHoraApertura());
        horario.setHoraCierre(dto.getHoraCierre());
        horario.setAbierto(dto.isAbierto());
        horario.setObservaciones(dto.getObservaciones());

        return horario;
    }
}
