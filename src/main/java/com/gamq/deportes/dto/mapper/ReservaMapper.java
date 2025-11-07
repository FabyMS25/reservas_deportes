package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.Reserva;

import java.util.stream.Collectors;

public class ReservaMapper {
    public static ReservaDto toDto(Reserva reserva) {
        ReservaDto reservaDto = new ReservaDto();

        // Basic identification
        reservaDto.setIdReserva(reserva.getIdReserva());
        reservaDto.setUuid(reserva.getUuid());
        reservaDto.setEstado(reserva.isEstado());
        reservaDto.setFechaCreacion(reserva.getFechaCreacion());
        reservaDto.setFechaActualizacion(reserva.getFechaActualizacion());

        // Reservation details
        reservaDto.setFechaReserva(reserva.getFechaReserva());
        reservaDto.setFechaAprobacion(reserva.getFechaAprobacion());
        reservaDto.setFechaRechazo(reserva.getFechaRechazo());
        reservaDto.setMotivoRechazo(reserva.getMotivoRechazo());
        reservaDto.setAprobadoPor(reserva.getAprobadoPor());
        reservaDto.setEstadoValidacion(reserva.getEstadoValidacion());

        // Reservation status and type
        reservaDto.setEstadoReserva(reserva.getEstadoReserva());
        reservaDto.setTipoReserva(reserva.getTipoReserva());
        reservaDto.setNumeroReserva(reserva.getNumeroReserva());
        reservaDto.setNumeroTasa(reserva.getNumeroTasa());
        reservaDto.setMontoTotal(reserva.getMontoTotal());
        reservaDto.setClasificadorRuatId(reserva.getClasificadorRuatId());

        // Free reservation details
        reservaDto.setEsReservaGratuita(reserva.getEsReservaGratuita());
        reservaDto.setMotivoGratuidad(reserva.getMotivoGratuidad());

        // Relations
        reservaDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(reserva.getCampoDeportivo(), false));
        reservaDto.setContribuyenteDto(ContribuyenteMapper.toDto(reserva.getContribuyente(), false));

        if (reserva.getOrganizacion() != null) {
            reservaDto.setOrganizacionDto(OrganizacionMapper.toDto(reserva.getOrganizacion(), false));
        }

        if (reserva.getEstudianteEscuela() != null) {
            reservaDto.setEstudianteEscuelaDto(EstudianteEscuelaMapper.toDto(reserva.getEstudianteEscuela(), false));
        }

        if (reserva.getTarifa() != null) {
            reservaDto.setTarifaDto(TarifaMapper.toDto(reserva.getTarifa(), false));
        }

        // Specific reservation types
        if (reserva.getReservaPuntual() != null) {
            reservaDto.setReservaPuntualDto(ReservaPuntualMapper.toDto(reserva.getReservaPuntual(),false));
        }

        if (reserva.getReservaRecurrente() != null) {
            reservaDto.setReservaRecurrenteDto(ReservaRecurrenteMapper.toDto(reserva.getReservaRecurrente(),false));
        }

        if (reserva.getReservaPeriodo() != null) {
            reservaDto.setReservaPeriodoDto(ReservaPeriodoMapper.toDto(reserva.getReservaPeriodo(),false));
        }

        // Payments and audits
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

    public static Reserva toEntity(ReservaDto reservaDto) {
        Reserva reserva = new Reserva();

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

        reserva.setEsReservaGratuita(reservaDto.isEsReservaGratuita());
        reserva.setMotivoGratuidad(reservaDto.getMotivoGratuidad());

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

        reserva.setEsReservaGratuita(reservaDto.isEsReservaGratuita());
        reserva.setMotivoGratuidad(reservaDto.getMotivoGratuidad());

        return reserva;
    }
}