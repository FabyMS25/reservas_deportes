package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.PagoDto;
import com.gamq.deportes.model.Pago;

public class PagoMapper {

    public static PagoDto toDto(Pago pago, boolean includeRelations) {
        if (pago == null) return null;
        PagoDto pagoDto = new PagoDto();
        pagoDto.setUuid(pago.getUuid());
        pagoDto.setEstado(pago.isEstado());
        pagoDto.setFechaCreacion(pago.getFechaCreacion());
        pagoDto.setFechaActualizacion(pago.getFechaActualizacion());

        pagoDto.setNumeroTasa(pago.getNumeroTasa());
        pagoDto.setTipoTasa(pago.getTipoTasa());
        pagoDto.setFolio(pago.getFolio());
        pagoDto.setFormaPago(pago.getFormaPago());
        pagoDto.setFechaPago(pago.getFechaPago());
        pagoDto.setEntidadFinanciera(pago.getEntidadFinanciera());
        pagoDto.setMontoPagado(pago.getMontoPagado());
        pagoDto.setObservaciones(pago.getObservaciones());
        pagoDto.setEstadoPago(pago.getEstadoPago());
        pagoDto.setUltimaConsulta(pago.getUltimaConsulta());
        pagoDto.setRespuestaCompletaRuat(pago.getRespuestaCompletaRuat());

        if (includeRelations && pago.getReserva() != null) {
            pagoDto.setReservaDto(ReservaMapper.toDto(pago.getReserva()));
        }

        return pagoDto;
    }

    public static Pago toEntity(PagoDto pagoDto) {
        if (pagoDto == null) return null;

        Pago pago = new Pago();
        pago.setUuid(pagoDto.getUuid());
        pago.setEstado(pagoDto.isEstado());
        pago.setFechaCreacion(pagoDto.getFechaCreacion());
        pago.setFechaActualizacion(pagoDto.getFechaActualizacion());

        pago.setNumeroTasa(pagoDto.getNumeroTasa());
        pago.setTipoTasa(pagoDto.getTipoTasa());
        pago.setFolio(pagoDto.getFolio());
        pago.setFormaPago(pagoDto.getFormaPago());
        pago.setFechaPago(pagoDto.getFechaPago());
        pago.setEntidadFinanciera(pagoDto.getEntidadFinanciera());
        pago.setMontoPagado(pagoDto.getMontoPagado());
        pago.setObservaciones(pagoDto.getObservaciones());
        pago.setEstadoPago(pagoDto.getEstadoPago());
        pago.setUltimaConsulta(pagoDto.getUltimaConsulta());
        pago.setRespuestaCompletaRuat(pagoDto.getRespuestaCompletaRuat());

        return pago;
    }
}
