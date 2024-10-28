package com.gamq.deportes.service;

import com.gamq.deportes.dto.contribuyenteDto;
import com.gamq.deportes.model.login.login;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface contribuyenteService {

    contribuyenteDto findByUuid(String uuid);
    contribuyenteDto findByEmail(String email);
    List<contribuyenteDto> obtenerContribuyentes();
    contribuyenteDto crearContribuyente(contribuyenteDto contribuyenteDto);
    contribuyenteDto actualizarContribuyente(contribuyenteDto contribuyenteDto);
    String eliminarContribuyente(String uuid);
    contribuyenteDto loginContribuyente(login login);

    String delete(String uuid);
}
