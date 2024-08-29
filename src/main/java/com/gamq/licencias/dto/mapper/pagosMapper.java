package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.pagosDto;
import com.gamq.licencias.model.pagos;

public class pagosMapper {
    public static pagosDto toDto(pagos pagos, boolean op){
        pagosDto pagosDto=new pagosDto();
        pagosDto.setUuid(pagos.getUuid());
        pagosDto.setFechaPago(pagos.getFechaPago());
        pagosDto.setMontoPago(pagos.getMontoPago());
        pagosDto.setNumeroTasa(pagos.getNumeroTasa());
        pagosDto.setEntidadFinanciera(pagos.getEntidadFinanciera());
        pagosDto.setFolio(pagos.getFolio());
        pagosDto.setFormaPago(pagos.getFormaPago());
        pagosDto.setObservacion(pagos.getObservacion());

        if(op){
        pagosDto.setReservasDto(reservasMapper.toDto(pagos.getReservas()));}
        return pagosDto;
    }

    public static pagos toEntity(pagosDto pagosDto){
        pagos pagos= new pagos();
        pagos.setUuid(pagosDto.getUuid());
        pagos.setFechaPago(pagosDto.getFechaPago());
        pagos.setMontoPago(pagosDto.getMontoPago());
        pagos.setNumeroTasa(pagosDto.getNumeroTasa());
        pagos.setEntidadFinanciera(pagosDto.getEntidadFinanciera());
        pagos.setFolio(pagosDto.getFolio());
        pagos.setFormaPago(pagosDto.getFormaPago());
        pagos.setObservacion(pagosDto.getObservacion());
        pagos.setEstado(pagosDto.isEstado());

        return pagos;
    }
}
