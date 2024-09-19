package com.gamq.licencias.service;

import com.gamq.licencias.dto.complejoDto;
import org.springframework.beans.factory.annotation.Autowired;
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
