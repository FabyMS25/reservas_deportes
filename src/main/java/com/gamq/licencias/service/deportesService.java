package com.gamq.licencias.service;

import com.gamq.licencias.dto.deporteDto;


import java.util.List;

public interface deportesService {

    deporteDto findByUuid(String uuid);
    List<deporteDto> obtenerDeportes();
    deporteDto crearDeporte(deporteDto deporteDto);
    deporteDto actualizarDeporte(deporteDto deporteDto);
    String eliminarDeporte(String uuid);
}
