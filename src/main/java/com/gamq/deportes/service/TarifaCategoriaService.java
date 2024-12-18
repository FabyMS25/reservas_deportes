package com.gamq.deportes.service;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TarifaCategoriaService {
    TarifaCategoriaDto findByUuid(String uuid);
    List<TarifaCategoriaDto> obtenerTarifaCategorias();
    TarifaCategoriaDto crearTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto);
    TarifaCategoriaDto actualizarTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto);
    String delete(String uuid);
}
