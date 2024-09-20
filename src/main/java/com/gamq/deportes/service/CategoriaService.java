package com.gamq.deportes.service;

import com.gamq.deportes.dto.CategoriaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoriaService {

    CategoriaDto findByUuid(String uuid);
    List<CategoriaDto> obtenerCategorias();
    CategoriaDto crearCategoria(CategoriaDto CategoriaDto);
    CategoriaDto actualizarCategoria(CategoriaDto CategoriaDto);
   // String eliminarCategoria(String uuid);
   String delete(String uuid);
}
