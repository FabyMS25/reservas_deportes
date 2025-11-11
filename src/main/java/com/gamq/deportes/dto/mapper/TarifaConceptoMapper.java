package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.TarifaConceptoDto;
import com.gamq.deportes.model.TarifaConcepto;

public class TarifaConceptoMapper {

    public static TarifaConceptoDto toDto(TarifaConcepto tarifaConcepto) {
        if (tarifaConcepto == null) return null;

        TarifaConceptoDto dto = new TarifaConceptoDto();
        dto.setUuid(tarifaConcepto.getUuid());
        dto.setEstado(tarifaConcepto.isEstado());
        dto.setFechaCreacion(tarifaConcepto.getFechaCreacion());

        dto.setConcepto(tarifaConcepto.getConcepto());
        dto.setTipo(tarifaConcepto.getTipo());
        dto.setDesde(tarifaConcepto.getDesde());
        dto.setHasta(tarifaConcepto.getHasta());
        dto.setValor(tarifaConcepto.getValor());
        dto.setActivo(tarifaConcepto.getActivo());

        return dto;
    }

    public static TarifaConcepto toEntity(TarifaConceptoDto dto) {
        if (dto == null) return null;

        TarifaConcepto tarifaConcepto = new TarifaConcepto();
        tarifaConcepto.setUuid(dto.getUuid());
        tarifaConcepto.setEstado(dto.isEstado());
        tarifaConcepto.setFechaCreacion(dto.getFechaCreacion());

        return updateEntityFromDto(dto, tarifaConcepto);
    }

    public static TarifaConcepto updateEntityFromDto(TarifaConceptoDto dto, TarifaConcepto tarifaConcepto) {
        if (dto == null || tarifaConcepto == null) return tarifaConcepto;

        tarifaConcepto.setConcepto(dto.getConcepto());
        tarifaConcepto.setTipo(dto.getTipo());
        tarifaConcepto.setDesde(dto.getDesde());
        tarifaConcepto.setHasta(dto.getHasta());
        tarifaConcepto.setValor(dto.getValor());
        tarifaConcepto.setActivo(dto.getActivo());

        return tarifaConcepto;
    }
}
