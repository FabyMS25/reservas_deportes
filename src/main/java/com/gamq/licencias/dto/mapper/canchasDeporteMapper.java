package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.canchasDeporteDto;
import com.gamq.licencias.model.canchaDeportes;

public class canchasDeporteMapper {
    public static canchasDeporteDto toDto(canchaDeportes canchasDep){
        canchasDeporteDto canchasDeporteDto= new canchasDeporteDto();
        canchasDeporteDto.setUuid(canchasDep.getUuid());
        canchasDeporteDto.setEstado(canchasDep.isEstado());
        canchasDeporteDto.setCanchasDto(canchasMapper.toDto(canchasDep.getCancha()));
        canchasDeporteDto.setDeporteDto(deporteMapper.toDto(canchasDep.getDeporte()));



        return canchasDeporteDto;
    }
    public static canchaDeportes toEntity(canchasDeporteDto canchasDeporteDto){
        canchaDeportes canchaDeportes= new canchaDeportes();
        canchaDeportes.setUuid(canchasDeporteDto.getUuid());
        canchaDeportes.setEstado(canchasDeporteDto.isEstado());

        return canchaDeportes;
    }
}
