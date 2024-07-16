package com.gamq.licencias.service;

import com.gamq.licencias.dto.TarifaCategoriaDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TarifaCategoriaService {
    TarifaCategoriaDto findByUuid(String uuid);
    List<TarifaCategoriaDto> obtenerTarifaCategorias();
    TarifaCategoriaDto crearTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto);
    TarifaCategoriaDto actualizarTarifaCategoria(TarifaCategoriaDto TarifaCategoriaDto);
}
