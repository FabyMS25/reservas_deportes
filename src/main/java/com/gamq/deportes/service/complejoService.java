package com.gamq.deportes.service;

import com.gamq.deportes.dto.complejoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface complejoService {

    complejoDto findByUuid(String uuid);
    List<complejoDto> obtenerComplejos();
    complejoDto crearComplejo(complejoDto complejoDto);
    complejoDto actualizarComplejo(complejoDto complejoDto);
    //String eliminarComplejo(String uuid);
    String deleteComplex(String uuid);

}
