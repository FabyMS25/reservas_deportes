package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.CategoriaDto;
import com.gamq.deportes.model.Categoria;

public class CategoriaMapper {
    public static CategoriaDto toDto(Categoria categoria){
        CategoriaDto categoriaDto=new CategoriaDto();
        categoriaDto.setUuid(categoria.getUuid());
        categoriaDto.setTipo(categoria.getTipo());
        categoriaDto.setCodigo(categoria.getCodigo());
        categoriaDto.setNombre(categoria.getNombre());
        categoriaDto.setForAsociation(categoria.isForAsociation());
        return categoriaDto;
    }
    public static Categoria toEntity (CategoriaDto categoriaDto){
        Categoria categoria= new Categoria();
        categoria.setUuid(categoriaDto.getUuid());
        categoria.setCodigo(categoriaDto.getCodigo());
        categoria.setTipo(categoriaDto.getTipo());
        categoria.setNombre(categoriaDto.getNombre());
        categoria.setForAsociation(categoria.isForAsociation());
        return categoria;
    }
}
