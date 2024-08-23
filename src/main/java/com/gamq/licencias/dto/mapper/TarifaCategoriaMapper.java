package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.TarifaCategoriaDto;
import com.gamq.licencias.model.TarifaCategoria;

public class TarifaCategoriaMapper {

    public static TarifaCategoriaDto toDto(TarifaCategoria tarifaCategoria, boolean op)
    {
        TarifaCategoriaDto tarifaCategoriaDto = new TarifaCategoriaDto();
        tarifaCategoriaDto.setUuid(tarifaCategoria.getUuid());
        tarifaCategoriaDto.setEstado(tarifaCategoria.isEstado());
        tarifaCategoriaDto.setCategoriaDto(CategoriaMapper.toDto(tarifaCategoria.getCategoria()));
//        tarifaCategoriaDto.setTarifaDto(TarifaMapper.toDto(tarifaCategoria.getTarifa(), true));

        if (op) {
            tarifaCategoriaDto.setTarifaDto(TarifaMapper.toDto(tarifaCategoria.getTarifa(), true));
        }

        return tarifaCategoriaDto;
    }
    public static TarifaCategoria toEntity(TarifaCategoriaDto tarifaCategoriaDto){
        TarifaCategoria tarifaCategoria= new TarifaCategoria();
        tarifaCategoria.setUuid(tarifaCategoriaDto.getUuid());
        tarifaCategoria.setEstado(tarifaCategoriaDto.isEstado());
        return tarifaCategoria;
    }
}
