package com.gamq.licencias.service;

import com.gamq.licencias.dto.canchasDeporteDto;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface canchasDeporteService {

    canchasDeporteDto findByUuid(String uuid);
    List<canchasDeporteDto> obtenerCanchasDeporte();
    canchasDeporteDto crearCanchaDeporte(canchasDeporteDto canchasDeporteDto);
    canchasDeporteDto actualizarCanchaDeporte(canchasDeporteDto canchasDeporteDto);
    String eliminarCanchasDeporte(String uuid);
}
