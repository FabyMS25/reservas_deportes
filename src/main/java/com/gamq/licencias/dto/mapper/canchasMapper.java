package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.canchas;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class canchasMapper {

    public static canchasDto toDto(canchas canchas){
        canchasDto canchasDto= new canchasDto();
        canchasDto.setUuid(canchas.getUuid());
        canchasDto.setCodigo(canchas.getCodigo());
        canchasDto.setTipo(canchas.getTipo());
        canchasDto.setEstado(canchas.isEstado());
        canchasDto.setComplejoDto(complejoMapper.toDto(canchas.getComplejo()));
        canchasDto.setSuperficieDto(SuperficieMapper.toDto(canchas.getSuperficie()));
        return canchasDto;
    }
    public static canchas toEntity(canchasDto canchasDto){
        canchas canchas =new canchas();
        canchas.setUuid(canchasDto.getUuid());
        canchas.setCodigo(canchasDto.getCodigo());
        canchas.setTipo(canchasDto.getTipo());
        canchas.setEstado(canchasDto.isEstado());
        return canchas;
    }


}