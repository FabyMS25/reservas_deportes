package com.gamq.deportes.service;

import com.gamq.deportes.dto.CampoDeportivoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CampoService {

    CampoDeportivoDto findByUuid(String uuid);
    List<CampoDeportivoDto> findCampoByComplejo(String uuidComplejo);
    List<CampoDeportivoDto> obtenerCampos();
    CampoDeportivoDto crearCampo(CampoDeportivoDto campoDto);
    CampoDeportivoDto actualizarCampo(CampoDeportivoDto canchasDto);
    String eliminarCampos(String uuid);
}
