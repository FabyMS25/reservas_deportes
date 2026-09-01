package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.model.Reserva;

import java.util.stream.Collectors;

public class ReservaMapper {
    public static ReservaDto    toDto(Reserva reserva) {
        ReservaDto reservaDto = new ReservaDto();

        // Basic identification
        reservaDto.setIdReserva(reserva.getIdReserva());
        reservaDto.setUuid(reserva.getUuid());
        reservaDto.setEstado(reserva.isEstado());
        reservaDto.setFechaCreacion(reserva.getFechaCreacion());
        reservaDto.setFechaActualizacion(reserva.getFechaActualizacion());

        // Reservation details
        reservaDto.setFechaReserva(reserva.getFechaReserva());
        reservaDto.setFechaLimitePago(reserva.getFechaLimitePago());
        reservaDto.setCancelacionAutomatica(reserva.getCancelacionAutomatica());
        reservaDto.setMotivoCancelacionAutomatica(reserva.getMotivoCancelacionAutomatica());
        reservaDto.setAutorizadoPor(reserva.getAutorizadoPor());
        reservaDto.setFechaAprobacion(reserva.getFechaAprobacion());
        reservaDto.setFechaRechazo(reserva.getFechaRechazo());
        reservaDto.setMotivoRechazo(reserva.getMotivoRechazo());
        reservaDto.setAprobadoPor(reserva.getAprobadoPor());
        reservaDto.setCreadoPor(reserva.getCreadoPor());
        reservaDto.setAnuladoPor(reserva.getAnuladoPor());
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
        reservaDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(reserva.getCampoDeportivo(), true));
        reservaDto.setContribuyenteDto(ContribuyenteMapper.toDto(reserva.getContribuyente(), false));

        if (reserva.getOrganizacion() != null) {
            reservaDto.setOrganizacionDto(OrganizacionMapper.toDto(reserva.getOrganizacion(), false));
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

        if (reserva.getReservasPeriodoList() != null && !reserva.getReservasPeriodoList().isEmpty()) {
            reservaDto.setReservasPeriodoDtoList(reserva.getReservasPeriodoList().stream()
                    .map(periodo -> ReservaPeriodoMapper.toDto(periodo, false))
                    .collect(Collectors.toList()));
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

        if (reserva.getTarifaDetallesList() != null && !reserva.getTarifaDetallesList().isEmpty()) {
            reservaDto.setTarifaDetallesDtoList(
                    reserva.getTarifaDetallesList().stream()
                            .map(detalle -> ReservaTarifaDetalleMapper.toDto(detalle, false))
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
        reserva.setFechaLimitePago(reservaDto.getFechaLimitePago());
        reserva.setCancelacionAutomatica(reservaDto.getCancelacionAutomatica());
        reserva.setMotivoCancelacionAutomatica(reservaDto.getMotivoCancelacionAutomatica());
        reserva.setAutorizadoPor(reservaDto.getAutorizadoPor());
        reserva.setFechaAprobacion(reservaDto.getFechaAprobacion());
        reserva.setFechaRechazo(reservaDto.getFechaRechazo());
        reserva.setMotivoRechazo(reservaDto.getMotivoRechazo());
        reserva.setAprobadoPor(reservaDto.getAprobadoPor());
        reserva.setCreadoPor(reservaDto.getCreadoPor());
        reserva.setAnuladoPor(reservaDto.getAnuladoPor());
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
        reserva.setFechaLimitePago(reservaDto.getFechaLimitePago());
        reserva.setCancelacionAutomatica(reservaDto.getCancelacionAutomatica());
        reserva.setMotivoCancelacionAutomatica(reservaDto.getMotivoCancelacionAutomatica());
        reserva.setAutorizadoPor(reservaDto.getAutorizadoPor());
        reserva.setFechaAprobacion(reservaDto.getFechaAprobacion());
        reserva.setFechaRechazo(reservaDto.getFechaRechazo());
        reserva.setMotivoRechazo(reservaDto.getMotivoRechazo());
        reserva.setAprobadoPor(reservaDto.getAprobadoPor());
        if (reserva.getCreadoPor() == null) {
            reserva.setCreadoPor(reservaDto.getCreadoPor());
        }
        reserva.setAnuladoPor(reservaDto.getAnuladoPor());
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
