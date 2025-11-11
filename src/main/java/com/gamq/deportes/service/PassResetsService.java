package com.gamq.deportes.service;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.PassDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PassResetsService {

    PassDto findByUuid(String uuid);
    PassDto findByToken(String token);
    ContribuyenteDto findUserByToken(String token);
    List<PassDto> obtenerPassResets();
    PassDto crearPassResets(PassDto PassDto);
    PassDto actualizarPassResets(PassDto PassDto);
}
