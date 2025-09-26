package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.Reserva;

import java.util.stream.Collectors;

public class reservasMapper {
    public static ReservaDto toDto(Reserva reservas){
        ReservaDto reservasDto=new ReservaDto();
        reservasDto.setUuid(reservas.getUuid());
        reservasDto.setFechaCreated(reservas.getFechaCreated());
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
        reservasDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(reservas.getCampoDeportivo(),true));
        reservasDto.setContribuyenteDto(contribuyentesMapper.toDto(reservas.getContribuyente()));
        reservasDto.setPagosDtoList(
                reservas.getPagosList().stream().map(pagos->{
                    return pagosMapper.toDto(pagos,false);
                }).collect(Collectors.toList())
        );
        return reservasDto;
    }

    public static Reserva toEntity(ReservaDto reservasDto){
        Reserva reservas=new Reserva();
        reservas.setUuid(reservasDto.getUuid());
        reservas.setFechaCreated(reservasDto.getFechaCreated());
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
