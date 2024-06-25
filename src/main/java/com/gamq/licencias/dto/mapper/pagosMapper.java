package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.pagosDto;
import com.gamq.licencias.model.pagos;

public class pagosMapper {
    public static pagosDto toDto(pagos pagos){
        pagosDto pagosDto=new pagosDto();
        pagosDto.setUuid(pagos.getUuid());
        pagosDto.setEstadoPago(pagos.getEstadoPago());
        pagosDto.setFechaPago(pagos.getFechaPago());
        pagosDto.setMontoPago(pagos.getMontoPago());
        pagosDto.setNroTasa(pagos.getNroTasa());
        pagosDto.setEstado(pagos.isEstado());

        return pagosDto;
    }

    public static pagos toEntity(pagosDto pagosDto){
        pagos pagos= new pagos();
        pagos.setUuid(pagosDto.getUuid());
        pagos.setEstadoPago(pagosDto.getEstadoPago());
        pagos.setFechaPago(pagosDto.getFechaPago());
        pagos.setMontoPago(pagosDto.getMontoPago());
        pagos.setNroTasa(pagosDto.getNroTasa());
        pagos.setEstado(pagosDto.isEstado());

        return pagos;
    }
}
