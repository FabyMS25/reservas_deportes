package com.gamq.licencias.service;

import com.gamq.licencias.dto.SuperficieDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SuperficieService {

    SuperficieDto findByUuid(String uuid);
    List<SuperficieDto> obtenerSuperficies();
    SuperficieDto crearSuperficie(SuperficieDto SuperficieDto);
    SuperficieDto actualizarSuperficie(SuperficieDto SuperficieDto);
    //String eliminarCanchas(String uuid);
}
