package com.gamq.deportes.service;

import com.gamq.deportes.dto.HorarioExcepcionDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScheduleExceptionService {

    HorarioExcepcionDto findByUuid(String uuid);
    List<HorarioExcepcionDto> obtenerHorarios();
    HorarioExcepcionDto crearHorario(HorarioExcepcionDto horarioDto);
    HorarioExcepcionDto actualizarHorario(HorarioExcepcionDto horarioDto);
//    String eliminarHorario(String uuid);
    String delete(String uuid);
}
