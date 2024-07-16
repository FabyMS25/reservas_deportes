package com.gamq.licencias.service;

import com.gamq.licencias.dto.CategoriaDto;
import com.gamq.licencias.dto.CategoriaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoriaService {

    CategoriaDto findByUuid(String uuid);
    List<CategoriaDto> obtenerCategorias();
    CategoriaDto crearCategoria(CategoriaDto CategoriaDto);
    CategoriaDto actualizarCategoria(CategoriaDto CategoriaDto);
   // String eliminarCategoria(String uuid);
}
