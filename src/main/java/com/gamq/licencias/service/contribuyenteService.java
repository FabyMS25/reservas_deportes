package com.gamq.licencias.service;

import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.dto.contribuyenteDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface contribuyenteService {

    contribuyenteDto findByUuid(String uuid);
    List<contribuyenteDto> obtenerContribuyentes();
    contribuyenteDto crearContribuyente(contribuyenteDto contribuyenteDto);
    contribuyenteDto actualizarContribuyente(contribuyenteDto contribuyenteDto);
    String eliminarContribuyente(String uuid);
}
