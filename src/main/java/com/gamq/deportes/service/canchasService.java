package com.gamq.deportes.service;

import com.gamq.deportes.dto.canchasDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface canchasService {

    canchasDto findByUuid(String uuid);
    List<canchasDto> findCanchasByComplejo(String uuidComplejo);
    List<canchasDto> obtenerCanchas();
    canchasDto crearCancha(canchasDto canchasDto);
    canchasDto actualizarCancha(canchasDto canchasDto);
    String deleteField(String uuid);
}
