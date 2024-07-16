package com.gamq.licencias.service;

import com.gamq.licencias.dto.encargadoComplejoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface encargadoCanchaService {

    encargadoComplejoDto findByUuid(String uuid);
    List<encargadoComplejoDto> obtenerEncargadosCanchas();
    encargadoComplejoDto crearEncargadoCancha(encargadoComplejoDto encargadoComplejoDto);
    encargadoComplejoDto actualizarEncargadoCancha(encargadoComplejoDto encargadoComplejoDto);
    String eliminarEncargadoCancha(String uuid);


}
