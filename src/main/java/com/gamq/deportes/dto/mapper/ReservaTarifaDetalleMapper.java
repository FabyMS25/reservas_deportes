package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaTarifaDetalleDto;
import com.gamq.deportes.model.ReservaTarifaDetalle;

public class ReservaTarifaDetalleMapper {

    public static ReservaTarifaDetalleDto toDto(ReservaTarifaDetalle detalle, boolean includeRelations) {
        if (detalle == null) return null;

        ReservaTarifaDetalleDto dto = new ReservaTarifaDetalleDto();
        dto.setIdReservaTarifaDetalle(detalle.getIdReservaTarifaDetalle());
        dto.setUuid(detalle.getUuid());
        dto.setEstado(detalle.isEstado());
        dto.setFechaCreacion(detalle.getFechaCreacion());
        dto.setTipoLinea(detalle.getTipoLinea());
        dto.setNombreConcepto(detalle.getNombreConcepto());
        dto.setTipoCobro(detalle.getTipoCobro());
        dto.setTipoCalculo(detalle.getTipoCalculo());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        dto.setReglaAplicada(detalle.getReglaAplicada());
        dto.setDatosSnapshot(detalle.getDatosSnapshot());

        if (includeRelations) {
            dto.setReservaDto(ReservaMapper.toDto(detalle.getReserva()));
            if (detalle.getTarifa() != null) {
                dto.setTarifaDto(TarifaMapper.toDto(detalle.getTarifa(), false));
            }
            if (detalle.getTarifaConcepto() != null) {
                dto.setTarifaConceptoDto(TarifaConceptoMapper.toDto(detalle.getTarifaConcepto()));
            }
        }

        return dto;
    }

    public static ReservaTarifaDetalle toEntity(ReservaTarifaDetalleDto dto) {
        ReservaTarifaDetalle detalle = new ReservaTarifaDetalle();
        detalle.setUuid(dto.getUuid());
        detalle.setEstado(dto.isEstado());
        detalle.setFechaCreacion(dto.getFechaCreacion());
        return updateEntityFromDto(dto, detalle);
    }

    public static ReservaTarifaDetalle updateEntityFromDto(ReservaTarifaDetalleDto dto, ReservaTarifaDetalle detalle) {
        if (dto == null || detalle == null) return detalle;

        detalle.setTipoLinea(dto.getTipoLinea());
        detalle.setNombreConcepto(dto.getNombreConcepto());
        detalle.setTipoCobro(dto.getTipoCobro());
        detalle.setTipoCalculo(dto.getTipoCalculo());
        detalle.setCantidad(dto.getCantidad());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        detalle.setSubtotal(dto.getSubtotal());
        detalle.setReglaAplicada(dto.getReglaAplicada());
        detalle.setDatosSnapshot(dto.getDatosSnapshot());

        return detalle;
    }
}
