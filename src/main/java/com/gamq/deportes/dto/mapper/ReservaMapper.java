package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.Reserva;

import java.util.stream.Collectors;

public class ReservaMapper {
    public static ReservaDto toDto(Reserva reserva){
        ReservaDto reservaDto=new ReservaDto();

        reservaDto.setUuid(reserva.getUuid());
        reservaDto.setEstado(reserva.isEstado());
        reservaDto.setFechaCreacion(reserva.getFechaCreacion());
        reservaDto.setFechaActualizacion(reserva.getFechaActualizacion());

        reservaDto.setFechaReserva(reserva.getFechaReserva());
        reservaDto.setFechaAprobacion(reserva.getFechaAprobacion());
        reservaDto.setFechaRechazo(reserva.getFechaRechazo());
        reservaDto.setMotivoRechazo(reserva.getMotivoRechazo());
        reservaDto.setAprobadoPor(reserva.getAprobadoPor());
        reservaDto.setEstadoValidacion(reserva.getEstadoValidacion());

        reservaDto.setEstadoReserva(reserva.getEstadoReserva());
        reservaDto.setTipoReserva(reserva.getTipoReserva());
        reservaDto.setNumeroReserva(reserva.getNumeroReserva());
        reservaDto.setNumeroTasa(reserva.getNumeroTasa());
        reservaDto.setMontoTotal(reserva.getMontoTotal());
        reservaDto.setClasificadorRuatId(String.valueOf(reserva.getClasificadorRuatId()));
//        reservasDto.setHoraEntrada(reservas.getHoraEntrada());
//        reservasDto.setHoraSalida(reservas.getHoraSalida());
//        reservasDto.setCategoria(reservas.getCategoria());
//        reservasDto.setSuperficie(reservas.getSuperficie());
//        reservasDto.setTotalPersonas(reservas.getTotalPersonas());

        reservaDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(reserva.getCampoDeportivo(), true));
        reservaDto.setContribuyenteDto(ContribuyenteMapper.toDto(reserva.getContribuyente(), false));
        if (reserva.getOrganizacion() != null) {
            reservaDto.setOrganizacionDto(OrganizacionMapper.toDto(reserva.getOrganizacion(), false));
        }
        if (reserva.getTarifa() != null) {
            reservaDto.setTarifaDto(TarifaMapper.toDto(reserva.getTarifa(), false));
        }

        if (reserva.getReservaPuntual() != null) {
            reservaDto.setReservaPuntualDto(ReservaPuntualMapper.toDto(reserva.getReservaPuntual(), false));
        }
        if (reserva.getReservaRecurrente() != null) {
            reservaDto.setReservaRecurrenteDto(ReservaRecurrenteMapper.toDto(reserva.getReservaRecurrente(), false));
        }

        if (reserva.getPagosList() != null && !reserva.getPagosList().isEmpty()) {
            reservaDto.setPagosDtoList(
                    reserva.getPagosList().stream()
                            .map(pago -> PagoMapper.toDto(pago, false))
                            .collect(Collectors.toList())
            );
        }
        if (reserva.getAuditoriasList() != null && !reserva.getAuditoriasList().isEmpty()) {
            reservaDto.setAuditoriasDtoList(
                    reserva.getAuditoriasList().stream()
                            .map(auditoria -> ReservaAuditoriaMapper.toDto(auditoria, false))
                            .collect(Collectors.toList())
            );
        }


        return reservaDto;
    }

    public static Reserva toEntity(ReservaDto reservaDto){
        Reserva reserva=new Reserva();
        reserva.setUuid(reservaDto.getUuid());
        reserva.setEstado(reservaDto.isEstado());
        reserva.setFechaCreacion(reservaDto.getFechaCreacion());
        reserva.setFechaActualizacion(reservaDto.getFechaActualizacion());

        reserva.setFechaReserva(reservaDto.getFechaReserva());
        reserva.setFechaAprobacion(reservaDto.getFechaAprobacion());
        reserva.setFechaRechazo(reservaDto.getFechaRechazo());
        reserva.setMotivoRechazo(reservaDto.getMotivoRechazo());
        reserva.setAprobadoPor(reservaDto.getAprobadoPor());
        reserva.setEstadoValidacion(reservaDto.getEstadoValidacion());

        reserva.setEstadoReserva(reservaDto.getEstadoReserva());
        reserva.setTipoReserva(reservaDto.getTipoReserva());
        reserva.setNumeroReserva(reservaDto.getNumeroReserva());
        reserva.setNumeroTasa(reservaDto.getNumeroTasa());
        reserva.setMontoTotal(reservaDto.getMontoTotal());
        reserva.setClasificadorRuatId(reservaDto.getClasificadorRuatId());
//        if (reservaDto.getClasificadorRuatId() != null) {
//            try {
//                reserva.setClasificadorRuatId(Integer.parseInt(reservaDto.getClasificadorRuatId()));
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid clasificadorRuatId format: " + reservaDto.getClasificadorRuatId());
//            }
//        }
//        reserva.setHoraEntrada(reservaDto.getHoraEntrada());
//        reserva.setHoraSalida(reservaDto.getHoraSalida());
//        reserva.setCategoria(reservaDto.getCategoria());
//        reserva.setSuperficie(reservaDto.getSuperficie());
//        reserva.setTotalPersonas(reservaDto.getTotalPersonas());

        return reserva;
    }


    public static Reserva updateEntityFromDto(ReservaDto reservaDto, Reserva reserva) {
        if (reservaDto == null || reserva == null) {
            return reserva;
        }
        reserva.setFechaActualizacion(reservaDto.getFechaActualizacion());

        reserva.setFechaReserva(reservaDto.getFechaReserva());
        reserva.setFechaAprobacion(reservaDto.getFechaAprobacion());
        reserva.setFechaRechazo(reservaDto.getFechaRechazo());
        reserva.setMotivoRechazo(reservaDto.getMotivoRechazo());
        reserva.setAprobadoPor(reservaDto.getAprobadoPor());
        reserva.setEstadoValidacion(reservaDto.getEstadoValidacion());

        reserva.setEstadoReserva(reservaDto.getEstadoReserva());
        reserva.setTipoReserva(reservaDto.getTipoReserva());
        reserva.setNumeroReserva(reservaDto.getNumeroReserva());
        reserva.setNumeroTasa(reservaDto.getNumeroTasa());
        reserva.setMontoTotal(reservaDto.getMontoTotal());
        reserva.setClasificadorRuatId(reservaDto.getClasificadorRuatId());

//        if (reservaDto.getClasificadorRuatId() != null) {
//            try {
//                reserva.setClasificadorRuatId(Integer.parseInt(reservaDto.getClasificadorRuatId()));
//            } catch (NumberFormatException e) {
//                throw new IllegalArgumentException("Invalid clasificadorRuatId format: " + reservaDto.getClasificadorRuatId());
//            }
//        }

        return reserva;
    }
}
