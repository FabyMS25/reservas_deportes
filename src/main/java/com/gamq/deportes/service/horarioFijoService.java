package com.gamq.deportes.service;

import com.gamq.deportes.dto.HorarioFijoDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface horarioFijoService {

    HorarioFijoDto findByUuid(String uuid);
    List<HorarioFijoDto> obtenerHorarios();
    HorarioFijoDto crearHorario(HorarioFijoDto horarioDto);
    HorarioFijoDto actualizarHorario(HorarioFijoDto horarioDto);
//    String eliminarHorario(String uuid);
    String delete(String uuid);
}
