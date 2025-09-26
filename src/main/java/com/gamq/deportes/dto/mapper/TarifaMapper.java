package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.model.Tarifa;
import java.util.stream.Collectors;

public class TarifaMapper {

    public static TarifaDto toDto(Tarifa tarifa, boolean op)
    {
        TarifaDto tarifaDto = new TarifaDto();
        tarifaDto.setUuid(tarifa.getUuid());
        tarifaDto.setDescripcion(tarifa.getDescripcion());
        tarifaDto.setDesde(tarifa.getDesde());
        tarifaDto.setHasta(tarifa.getHasta());
        tarifaDto.setCosto(tarifa.getCosto());
        tarifaDto.setEstado(tarifa.isEstado());
        if (op) {
            tarifaDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(tarifa.getCampoDeportivo(),true));
        }

        tarifaDto.setCategoriaDtoList(
                tarifa.getCategoriaDtoList().stream().map(categoria -> {
                    return TarifaCategoriaMapper.toDto(categoria, false);
                }).collect(Collectors.toList())
        );
        return tarifaDto;
    }

    public static Tarifa toEntity (TarifaDto tarifaDto)
    {
        Tarifa tarifas = new Tarifa();
        tarifas.setUuid(tarifaDto.getUuid());
        tarifas.setDescripcion(tarifaDto.getDescripcion());
        tarifas.setDesde(tarifaDto.getDesde());
        tarifas.setHasta(tarifaDto.getHasta());
        tarifas.setCosto(tarifaDto.getCosto());
        tarifas.setEstado(tarifaDto.isEstado());

        // Map categories
//        if (tarifaDto.getCategoriaDtoList() != null) {
//            tarifas.setCategoriasList(tarifaDto.getCategoriaDtoList().stream()
//                    .map(categoriaDto -> {
//                        TarifaCategoria tarifaCategoria = TarifaCategoriaMapper.toEntity(categoriaDto);
//                        tarifaCategoria.setTarifa(tarifas);
//                        return tarifaCategoria;
//                    })
//                    .collect(Collectors.toList()));
//        }

        return tarifas;
    }

}
