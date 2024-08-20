package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.reservasDto;
import com.gamq.licencias.model.reservas;

public class reservasMapper {
    public static reservasDto toDto(reservas reservas){
        reservasDto reservasDto=new reservasDto();
        reservasDto.setUuid(reservas.getUuid());
        reservasDto.setFechaReserva(reservas.getFechaReserva());
        reservasDto.setHoraEntrada(reservas.getHoraEntrada());
        reservasDto.setHoraSalida(reservas.getHoraSalida());
        reservasDto.setTotalPago(reservas.getTotalPago());
        reservasDto.setCategoria(reservas.getCategoria());
        reservasDto.setSuperficie(reservas.getSuperficie());
        reservasDto.setTotalPersonas(reservas.getTotalPersonas());
        reservasDto.setEstado(reservas.isEstado());
        reservasDto.setNumeroTasa(reservas.getNumeroTasa());
        reservasDto.setEstadoReserva(reservas.getEstadoReserva());
        reservasDto.setCanchasDto(canchasMapper.toDto(reservas.getCancha()));
        reservasDto.setContribuyenteDto(contribuyentesMapper.toDto(reservas.getContribuyente()));
        if (reservas.getPago()!=null) {
            reservasDto.setPagosDto(pagosMapper.toDto(reservas.getPago()));
        }
        return reservasDto;
    }

    public static reservas toEntity(reservasDto reservasDto){
        reservas reservas=new reservas();
        reservas.setUuid(reservasDto.getUuid());
        reservas.setFechaReserva(reservasDto.getFechaReserva());
        reservas.setHoraEntrada(reservasDto.getHoraEntrada());
        reservas.setHoraSalida(reservasDto.getHoraSalida());
        reservas.setTotalPago(reservasDto.getTotalPago());
        reservas.setCategoria(reservasDto.getCategoria());
        reservas.setSuperficie(reservasDto.getSuperficie());
        reservas.setTotalPersonas(reservasDto.getTotalPersonas());
        reservas.setEstado(reservasDto.isEstado());
        reservas.setNumeroTasa(reservasDto.getNumeroTasa());
        reservas.setEstadoReserva(reservasDto.getEstadoReserva());

        return reservas;
    }
}
