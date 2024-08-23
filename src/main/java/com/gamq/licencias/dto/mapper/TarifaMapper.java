package com.gamq.licencias.dto.mapper;

import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.model.Tarifas;
import java.util.stream.Collectors;

public class TarifaMapper {

    public static TarifaDto toDto(Tarifas tarifa, boolean op)
    {
        TarifaDto tarifaDto = new TarifaDto();
        tarifaDto.setUuid(tarifa.getUuid());
        tarifaDto.setDescripcion(tarifa.getDescripcion());
        tarifaDto.setDesde(tarifa.getDesde());
        tarifaDto.setHasta(tarifa.getHasta());
        tarifaDto.setCosto(tarifa.getCosto());
        tarifaDto.setEstado(tarifa.isEstado());
        if (op) {
            tarifaDto.setCanchasDto(canchasMapper.toDto(tarifa.getCancha()));
        }

        tarifaDto.setCategoriaDtoList(
                tarifa.getCategoriasList().stream().map(categoria -> {
                    return TarifaCategoriaMapper.toDto(categoria, false);
                }).collect(Collectors.toList())
        );
        return tarifaDto;
    }

    public static Tarifas toEntity (TarifaDto tarifaDto)
    {
        Tarifas tarifas = new Tarifas();
        tarifas.setUuid(tarifaDto.getUuid());
        tarifas.setDescripcion(tarifaDto.getDescripcion());
        tarifas.setDesde(tarifaDto.getDesde());
        tarifas.setHasta(tarifaDto.getHasta());
        tarifas.setCosto(tarifaDto.getCosto());
        tarifas.setEstado(tarifaDto.isEstado());
        return  tarifas;
    }

}
