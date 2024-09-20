package com.gamq.deportes.service;

import com.gamq.deportes.dto.horarioDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface horarioService {

    horarioDto findByUuid(String uuid);
    List<horarioDto> obtenerHorarios();
    horarioDto crearHorario(horarioDto horarioDto);
    horarioDto actualizarHorario(horarioDto horarioDto);
//    String eliminarHorario(String uuid);
    String delete(String uuid);
}
