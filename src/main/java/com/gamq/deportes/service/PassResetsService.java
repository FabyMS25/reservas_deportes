package com.gamq.deportes.service;

import com.gamq.deportes.dto.PassDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PassResetsService {

    PassDto findByUuid(String uuid);
    List<PassDto> obtenerPassResets();
    PassDto crearPassResets(PassDto PassDto);
    PassDto actualizarPassResets(PassDto PassDto);
}
