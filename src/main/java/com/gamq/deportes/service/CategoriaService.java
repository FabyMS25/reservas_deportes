package com.gamq.deportes.service;

import com.gamq.deportes.dto.CategoriaDeportivaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoriaService {

    CategoriaDeportivaDto findByUuid(String uuid);
    List<CategoriaDeportivaDto> obtenerCategorias();
    CategoriaDeportivaDto crearCategoria(CategoriaDeportivaDto CategoriaDto);
    CategoriaDeportivaDto actualizarCategoria(CategoriaDeportivaDto CategoriaDto);
   // String eliminarCategoria(String uuid);
   String delete(String uuid);
}
