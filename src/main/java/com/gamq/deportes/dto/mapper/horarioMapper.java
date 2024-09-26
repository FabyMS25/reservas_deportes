package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.horarioDto;
import com.gamq.deportes.model.horario;

public class horarioMapper {

    public static horarioDto toDTo(horario horario, boolean op){
        horarioDto horarioDto=new horarioDto();
        horarioDto.setUuid(horario.getUuid());
        horarioDto.setDia(horario.getDia());
        horarioDto.setHoraFin(horario.getHoraFin());
        horarioDto.setHoraInicio(horario.getHoraInicio());
        horarioDto.setAbierto(horario.isAbierto());
        horarioDto.setEstado(horario.isEstado());
        if(op) {
            horarioDto.setCanchasDto(canchasMapper.toDto(horario.getCancha(),true));
        }
        return horarioDto;
    }
    public static horario toEntity(horarioDto horarioDto){
        horario horario=new horario();
        horario.setUuid(horarioDto.getUuid());
        horario.setDia(horarioDto.getDia());
        horario.setHoraFin(horarioDto.getHoraFin());
        horario.setHoraInicio(horarioDto.getHoraInicio());
        horario.setAbierto(horarioDto.isAbierto());
        horario.setEstado(horarioDto.isEstado());

        return horario;
    }
}
