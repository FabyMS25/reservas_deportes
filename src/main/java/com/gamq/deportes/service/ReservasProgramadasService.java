package com.gamq.deportes.service;

import com.gamq.deportes.dto.ReservasProgramadasDto;
import com.gamq.deportes.model.ReservasProgramadas;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ReservasProgramadasService {

    ReservasProgramadasDto findByUuid(String uuid);
    List<ReservasProgramadasDto> obtenerReservasProgramadas();
    ReservasProgramadasDto crearReservasProgramadas(ReservasProgramadasDto ReservasProgramadasDto);
    ReservasProgramadasDto actualizarReservasProgramadas(ReservasProgramadasDto ReservasProgramadasDto);
    //String deleteField(String uuid);
}
