package com.gamq.licencias.service;

import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.dto.encargadoComplejoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface encargadoComplejoService {

    encargadoComplejoDto findByUuid(String uuid);
    encargadoComplejoDto findByUserUuid(String userUuid);
    List<encargadoComplejoDto> obtenerEncargadosComplejos();
    encargadoComplejoDto crearEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto);
    encargadoComplejoDto actualizarEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto);
    //String eliminarEncargadoCancha(String uuid);
    String delete(String uuid);

}
