package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.horarioDto;
import com.gamq.licencias.model.horario;

public class horarioMapper {

    public static horarioDto toDTo(horario horario){
        horarioDto horarioDto=new horarioDto();
        horarioDto.setUuid(horario.getUuid());
        horarioDto.setDia(horario.getDia());
        horarioDto.setHoraFin(horario.getHoraFin());
        horarioDto.setHoraInicio(horario.getHoraInicio());
        horarioDto.setEstado(horario.isEstado());
        horarioDto.setCanchasDto(canchasMapper.toDto(horario.getCancha()));

        return horarioDto;
    }
    public static horario toEntity(horarioDto horarioDto){
        horario horario=new horario();
        horario.setUuid(horarioDto.getUuid());
        horario.setDia(horarioDto.getDia());
        horario.setHoraFin(horarioDto.getHoraFin());
        horario.setHoraInicio(horarioDto.getHoraInicio());
        horario.setEstado(horarioDto.isEstado());

        return horario;
    }
}
