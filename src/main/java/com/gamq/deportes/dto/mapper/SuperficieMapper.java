package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.SuperficieDto;
import com.gamq.deportes.model.Superficie;

public class SuperficieMapper {

    public static SuperficieDto toDto(Superficie superficie){
        SuperficieDto superficieDto = new SuperficieDto();
        superficieDto.setUuid(superficie.getUuid());
        superficieDto.setDescripcion(superficie.getDescripcion());
        superficieDto.setEstado(superficie.isEstado());
        return  superficieDto;
    }
    public static Superficie toEntity(SuperficieDto superficieDto){
        Superficie superficie= new Superficie();
        superficie.setUuid(superficieDto.getUuid());
        superficie.setDescripcion(superficieDto.getDescripcion());
        superficie.setEstado(superficieDto.isEstado());

        return superficie;
    }
}
