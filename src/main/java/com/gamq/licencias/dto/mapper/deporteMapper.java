package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.deporteDto;
import com.gamq.licencias.model.deportes;

public class deporteMapper {
    public static deporteDto toDto(deportes deporte){
        deporteDto deporteDto = new deporteDto();
        deporteDto.setUuid(deporte.getUuid());
        deporteDto.setNombre(deporte.getNombre());
        deporteDto.setEstado(deporte.isEstado());

        return deporteDto;
    }

    public static deportes toEntity(deporteDto deporteDto){
        deportes deportes =new deportes();
        deportes.setUuid(deporteDto.getUuid());
        deportes.setNombre(deporteDto.getNombre());
        deportes.setEstado(deporteDto.isEstado());

        return deportes;
    }
}
