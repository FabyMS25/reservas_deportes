package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservasProgramadasDto;
import com.gamq.deportes.model.ReservasProgramadas;

public class ReservasProgramadasMapper {
    public static ReservasProgramadasDto toDto(ReservasProgramadas reservasProgramadas){
        ReservasProgramadasDto reservaDto=new ReservasProgramadasDto();
        reservaDto.setUuid(reservasProgramadas.getUuid());
        reservaDto.setFechaCreated(reservasProgramadas.getFechaCreated());
        reservaDto.setFechaReserva(reservasProgramadas.getFechaReserva());
        reservaDto.setSuperficie(reservasProgramadas.getSuperficie());
        reservaDto.setCategoria(reservasProgramadas.getCategoria());
        reservaDto.setTotalPersonas(reservasProgramadas.getTotalPersonas());
        reservaDto.setEstadoReserva(reservasProgramadas.getEstadoReserva());
        reservaDto.setEstado(reservasProgramadas.isEstado());
        reservaDto.setNumeroTasa(reservasProgramadas.getNumeroTasa());
        reservaDto.setCanchasDto(canchasMapper.toDto(reservasProgramadas.getCancha(),false));
        reservaDto.setContribuyenteDto(contribuyentesMapper.toDto(reservasProgramadas.getContribuyente()));
        //reservaDto.setTableroDto();

        return reservaDto;
    }
    public static ReservasProgramadas toEntity(ReservasProgramadasDto reservasProgramadasDto){
        ReservasProgramadas reserva= new ReservasProgramadas();
        reserva.setUuid(reservasProgramadasDto.getUuid());
        reserva.setFechaCreated(reservasProgramadasDto.getFechaCreated());
        reserva.setFechaReserva(reservasProgramadasDto.getFechaReserva());
        reserva.setSuperficie(reservasProgramadasDto.getSuperficie());
        reserva.setCategoria(reservasProgramadasDto.getCategoria());
        reserva.setTotalPersonas(reservasProgramadasDto.getTotalPersonas());
        reserva.setEstadoReserva(reservasProgramadasDto.getEstadoReserva());
        reserva.setEstado(reservasProgramadasDto.isEstado());
        reserva.setNumeroTasa(reservasProgramadasDto.getNumeroTasa());
        reserva.setCancha(canchasMapper.toEntity(reservasProgramadasDto.getCanchasDto()));
        reserva.setContribuyente(contribuyentesMapper.toEntity(reservasProgramadasDto.getContribuyenteDto()));


        return reserva;

    }
}
