package com.gamq.licencias.service;

import com.gamq.licencias.dto.canchasDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface canchasService {

    canchasDto findByUuid(String uuid);
    List<canchasDto> obtenerCanchas();
    canchasDto crearCancha(canchasDto canchasDto);
    canchasDto actualizarCancha(canchasDto canchasDto);
    String eliminarCanchas(String uuid);
}
