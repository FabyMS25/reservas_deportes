package com.gamq.deportes.service;

import com.gamq.deportes.dto.EncargadoComplejoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EncargadoComplejoService {

    EncargadoComplejoDto findByUuid(String uuid);
    EncargadoComplejoDto findByUserUuid(String userUuid);
    List<EncargadoComplejoDto> obtenerEncargadosComplejos();
    EncargadoComplejoDto crearEncargadoComplejo(EncargadoComplejoDto encargadoComplejoDto);
    EncargadoComplejoDto actualizarEncargadoComplejo(EncargadoComplejoDto encargadoComplejoDto);
    //String eliminarEncargadoCancha(String uuid);
    String delete(String uuid);

}
