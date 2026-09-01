package com.gamq.deportes.service;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.model.login.login;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ContribuyenteService {

    ContribuyenteDto findByUuid(String uuid);
    ContribuyenteDto findByEmail(String email);
    List<ContribuyenteDto> obtenerContribuyentes();
    ContribuyenteDto crearContribuyente(ContribuyenteDto contribuyenteDto);
    ContribuyenteDto actualizarContribuyente(ContribuyenteDto contribuyenteDto);
    String eliminarContribuyente(String uuid);
    ContribuyenteDto loginContribuyente(login login);
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);

    String delete(String uuid);
}
