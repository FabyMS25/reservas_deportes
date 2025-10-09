package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.CategoriaDeportivaDto;
import com.gamq.deportes.model.CategoriaDeportiva;

public class CategoriaMapper {
    public static CategoriaDeportivaDto toDto(CategoriaDeportiva categoria) {
        CategoriaDeportivaDto categoriaDto = new CategoriaDeportivaDto();
        categoriaDto.setUuid(categoria.getUuid());
        categoriaDto.setFechaCreacion(categoria.getFechaCreacion());
        categoriaDto.setEstado(categoria.isEstado());
        categoriaDto.setCodigo(categoria.getCodigo());
        categoriaDto.setNombre(categoria.getNombre());
        categoriaDto.setDescripcion(categoria.getDescripcion());
        categoriaDto.setForAsociacion(categoria.isForAsociacion());
        categoriaDto.setTipo(categoria.getTipo());
        categoriaDto.setActivo(categoria.isActivo());
//        categoriaDto.setRequiereAprobacion(categoria.getRequiereAprobacion());
//        categoriaDto.setMaxDuracionAutoAprobacion(categoria.getMaxDuracionAutoAprobacion());
//        categoriaDto.setMaxPersonasAutoAprobacion(categoria.getMaxPersonasAutoAprobacion());
        return categoriaDto;
    }
    public static CategoriaDeportiva toEntity(CategoriaDeportivaDto categoriaDto) {
        CategoriaDeportiva categoria = new CategoriaDeportiva();
        categoria.setUuid(categoriaDto.getUuid());
        categoria.setEstado(categoriaDto.isEstado());
        categoria.setFechaCreacion(categoriaDto.getFechaCreacion());
        categoria.setCodigo(categoriaDto.getCodigo());
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setTipo(categoriaDto.getTipo());
        categoria.setDescripcion(categoriaDto.getDescripcion());
        categoria.setForAsociacion(categoriaDto.isForAsociacion());
        categoria.setActivo(categoriaDto.isActivo());
        return categoria;
    }

    public static CategoriaDeportiva updateEntityFromDto(CategoriaDeportivaDto categoriaDto, CategoriaDeportiva categoria) {
        if (categoriaDto == null || categoria == null) return categoria;

        categoria.setCodigo(categoriaDto.getCodigo());
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setTipo(categoriaDto.getTipo());
        categoria.setDescripcion(categoriaDto.getDescripcion());
        categoria.setForAsociacion(categoriaDto.isForAsociacion());
        categoria.setActivo(categoriaDto.isActivo());
//        categoria.setRequiereAprobacion(categoriaDto.getRequiereAprobacion());
//        categoria.setMaxDuracionAutoAprobacion(categoriaDto.getMaxDuracionAutoAprobacion());
//        categoria.setMaxPersonasAutoAprobacion(categoriaDto.getMaxPersonasAutoAprobacion());

        return categoria;
    }

}
