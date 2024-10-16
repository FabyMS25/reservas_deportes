package com.gamq.deportes.service;

import com.gamq.deportes.dto.PassResetsDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PassResetsService {

    PassResetsDto findByUuid(String uuid);
    List<PassResetsDto> obtenerPassResets();
    PassResetsDto crearPassResets(PassResetsDto PassResetsDto);
    PassResetsDto actualizarPassResets(PassResetsDto PassResetsDto);
}
