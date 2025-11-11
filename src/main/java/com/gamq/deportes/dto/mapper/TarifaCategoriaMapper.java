package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.model.TarifaCategoria;

public class TarifaCategoriaMapper {

    public static TarifaCategoriaDto toDto(TarifaCategoria tarifaCategoria, boolean includeRelations) {
        TarifaCategoriaDto dto = new TarifaCategoriaDto();
        dto.setUuid(tarifaCategoria.getUuid());
        dto.setEstado(tarifaCategoria.isEstado());
        dto.setCategoriaDto(CategoriaMapper.toDto(tarifaCategoria.getCategoria()));
        if (includeRelations) {
            dto.setTarifaDto(TarifaMapper.toDto(tarifaCategoria.getTarifa(), true));
        }
        return dto;
    }

    public static TarifaCategoria toEntity(TarifaCategoriaDto dto) {
        TarifaCategoria tarifaCategoria = new TarifaCategoria();
        tarifaCategoria.setUuid(dto.getUuid());
        tarifaCategoria.setEstado(dto.isEstado());

        return tarifaCategoria;
    }

}
