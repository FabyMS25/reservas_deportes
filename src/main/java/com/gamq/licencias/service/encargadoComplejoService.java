package com.gamq.licencias.service;

import com.gamq.licencias.dto.encargadoComplejoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface encargadoComplejoService {

    encargadoComplejoDto findByUuid(String uuid);
    List<encargadoComplejoDto> obtenerEncargadosComplejos();
    encargadoComplejoDto crearEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto);
    encargadoComplejoDto actualizarEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto);
    //String eliminarEncargadoCancha(String uuid);


}
