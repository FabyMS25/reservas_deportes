package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.model.Tarifa;
import java.util.stream.Collectors;

public class TarifaMapper {

    public static TarifaDto toDto(Tarifa tarifa, boolean includeRelations) {
        TarifaDto tarifaDto = new TarifaDto();
        tarifaDto.setUuid(tarifa.getUuid());
        tarifaDto.setEstado(tarifa.isEstado());
        tarifaDto.setFechaCreacion(tarifa.getFechaCreacion());

        tarifaDto.setPrecioBase(tarifa.getPrecioBase());
        tarifaDto.setFechaInicio(tarifa.getFechaInicio());
        tarifaDto.setFechaFin(tarifa.getFechaFin());
        tarifaDto.setDesde(tarifa.getDesde());
        tarifaDto.setHasta(tarifa.getHasta());
        tarifaDto.setActivo(tarifa.getActivo());
        tarifaDto.setDescripcion(tarifa.getDescripcion());
        tarifaDto.setObservaciones(tarifa.getObservaciones());
        tarifaDto.setTipoCobro(tarifa.getTipoCobro());
        tarifaDto.setFrecuencia(tarifa.getFrecuencia());
        tarifaDto.setSesionesIncluidas(tarifa.getSesionesIncluidas());
        if (tarifa.getConceptos() != null) {
            tarifaDto.setConceptosDtos(tarifa.getConceptos().stream()
                    .map(TarifaConceptoMapper::toDto)
                    .collect(Collectors.toList()));
        }
        if (includeRelations) {
            tarifaDto.setCampoDeportivoDto(CampoDeportivoMapper.toDto(tarifa.getCampoDeportivo(), true));

        }
        if (tarifa.getClasificadorRuat() != null) {
            tarifaDto.setClasificadorRuatDto(ClasificadorRuatMapper.toDto(tarifa.getClasificadorRuat()));
        }
//        if (tarifa.getCategoriasList() != null) {
//            tarifaDto.setCategoriasDtoList(tarifa.getCategoriasList().stream()
//                    .map(categoria -> TarifaCategoriaMapper.toDto(categoria, false))
//                    .collect(Collectors.toList()));
//        }
        tarifaDto.setCategoriasDtoList(tarifa.getCategoriasList().stream()
                .map(categoria -> {return TarifaCategoriaMapper.toDto(categoria, false);
                }).collect(Collectors.toList())
        );
        return tarifaDto;
    }

    public static Tarifa toEntity(TarifaDto tarifaDto) {
        Tarifa tarifa = new Tarifa();
        tarifa.setUuid(tarifaDto.getUuid());
        tarifa.setEstado(tarifaDto.isEstado());
        tarifa.setFechaCreacion(tarifaDto.getFechaCreacion());
        // Core tarifa fields
        tarifa.setPrecioBase(tarifaDto.getPrecioBase());
        tarifa.setFechaInicio(tarifaDto.getFechaInicio());
        tarifa.setFechaFin(tarifaDto.getFechaFin());
        tarifa.setDesde(tarifaDto.getDesde());
        tarifa.setHasta(tarifaDto.getHasta());
        tarifa.setActivo(tarifaDto.getActivo());
        tarifa.setDescripcion(tarifaDto.getDescripcion());
        tarifa.setObservaciones(tarifaDto.getObservaciones());
        tarifa.setTipoCobro(tarifaDto.getTipoCobro());
        tarifa.setFrecuencia(tarifaDto.getFrecuencia());
        tarifa.setSesionesIncluidas(tarifaDto.getSesionesIncluidas());

        return tarifa;
    }

    public static Tarifa updateEntityFromDto(TarifaDto tarifaDto, Tarifa tarifa) {
        tarifa.setPrecioBase(tarifaDto.getPrecioBase());
        tarifa.setFechaInicio(tarifaDto.getFechaInicio());
        tarifa.setFechaFin(tarifaDto.getFechaFin());
        tarifa.setDesde(tarifaDto.getDesde());
        tarifa.setHasta(tarifaDto.getHasta());
        tarifa.setActivo(tarifaDto.getActivo());
        tarifa.setDescripcion(tarifaDto.getDescripcion());
        tarifa.setObservaciones(tarifaDto.getObservaciones());
        tarifa.setTipoCobro(tarifaDto.getTipoCobro());
        tarifa.setFrecuencia(tarifaDto.getFrecuencia());
        tarifa.setSesionesIncluidas(tarifaDto.getSesionesIncluidas());
        return tarifa;
    }
}
