package com.gamq.deportes.service;

import com.gamq.deportes.dto.ComplejoDeportivoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ComplejoService {

    ComplejoDeportivoDto findByUuid(String uuid);
    List<ComplejoDeportivoDto> obtenerComplejos();
    ComplejoDeportivoDto crearComplejo(ComplejoDeportivoDto complejoDto);
    ComplejoDeportivoDto actualizarComplejo(ComplejoDeportivoDto complejoDto);
    String eliminarComplejo(String uuid);

}
