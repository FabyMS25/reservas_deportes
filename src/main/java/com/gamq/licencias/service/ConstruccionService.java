package com.gamq.licencias.service;

import com.gamq.licencias.dto.ConstruccionDto;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface ConstruccionService {
    ConstruccionDto findByUuid(String uuid);
    List<ConstruccionDto> obtenerConstrucciones();
    ConstruccionDto crearConstruccion(ConstruccionDto construccionDto);
    ConstruccionDto actualizarConstruccion(ConstruccionDto construccionDto);
    String eliminarConstruccion(String uuid);


}
