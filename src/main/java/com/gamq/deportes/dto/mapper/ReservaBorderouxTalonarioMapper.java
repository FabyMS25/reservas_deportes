package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ReservaBorderouxTalonarioDto;
import com.gamq.deportes.model.ReservaBorderouxTalonario;

public class ReservaBorderouxTalonarioMapper {

    private ReservaBorderouxTalonarioMapper() {
    }

    public static ReservaBorderouxTalonarioDto toDto(ReservaBorderouxTalonario talonario) {
        if (talonario == null) {
            return null;
        }
        ReservaBorderouxTalonarioDto dto = new ReservaBorderouxTalonarioDto();
        dto.setIdReservaBorderouxTalonario(talonario.getIdReservaBorderouxTalonario());
        dto.setUuid(talonario.getUuid());
        dto.setEstado(talonario.isEstado());
        dto.setFechaCreacion(talonario.getFechaCreacion());
        dto.setPrecioBoleta(talonario.getPrecioBoleta());
        dto.setNumeroInicial(talonario.getNumeroInicial());
        dto.setNumeroFinal(talonario.getNumeroFinal());
        dto.setCantidad(talonario.getCantidad());
        dto.setTotalBoletas(talonario.getTotalBoletas());
        dto.setMontoBorderoux(talonario.getMontoBorderoux());
        return dto;
    }

    public static ReservaBorderouxTalonario toEntity(ReservaBorderouxTalonarioDto dto) {
        if (dto == null) {
            return null;
        }
        ReservaBorderouxTalonario talonario = new ReservaBorderouxTalonario();
        talonario.setUuid(dto.getUuid());
        talonario.setEstado(dto.isEstado());
        talonario.setPrecioBoleta(dto.getPrecioBoleta());
        talonario.setNumeroInicial(dto.getNumeroInicial());
        talonario.setNumeroFinal(dto.getNumeroFinal());
        talonario.setCantidad(dto.getCantidad());
        talonario.setTotalBoletas(dto.getTotalBoletas());
        talonario.setMontoBorderoux(dto.getMontoBorderoux());
        return talonario;
    }
}
