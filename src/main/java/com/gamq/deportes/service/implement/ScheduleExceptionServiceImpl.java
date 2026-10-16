package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.HorarioExcepcionDto;
import com.gamq.deportes.dto.mapper.HorarioExcepcionMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.model.HorarioExcepcion;
import com.gamq.deportes.repository.CampoDeportivoRepository;
import com.gamq.deportes.repository.HorarioExcepcionRepository;
import com.gamq.deportes.service.ScheduleExceptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ScheduleExceptionServiceImpl implements ScheduleExceptionService {
    
    @Autowired
    HorarioExcepcionRepository horarioRepository;

    @Autowired
    CampoDeportivoRepository canchasRepository;
    
    public HorarioExcepcionDto findByUuid(String uuid){

        Optional<HorarioExcepcion> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            return HorarioExcepcionMapper.toDto(horarioOptional.get(), true);
        } else {
            throw new NotFoundException(HORARIOEXCEPCION , uuid);
        }

    }
    public List<HorarioExcepcionDto> obtenerHorarios(){

        List<HorarioExcepcion> horarios = horarioRepository.findAll();

        return horarios.stream().map(horario -> {
            return HorarioExcepcionMapper.toDto(horario,true);
        }).collect(Collectors.toList());
    }

    public HorarioExcepcionDto crearHorario(HorarioExcepcionDto horarioDto){

        if (horarioDto != null) {

            HorarioExcepcion newHorario = HorarioExcepcionMapper.toEntity(horarioDto);
            Optional<CampoDeportivo> cancha=canchasRepository.findByUuid(horarioDto.getCampoDeportivoDto().getUuid());
            if (cancha.isPresent())
                newHorario.setCampoDeportivo(cancha.get());
                HorarioExcepcion horarioNew= horarioRepository.save(newHorario);
            return HorarioExcepcionMapper.toDto(horarioNew,true);
        } else {
            throw new NullReferenceException(HORARIOEXCEPCION);
        }
    }
    public HorarioExcepcionDto actualizarHorario(HorarioExcepcionDto horarioDto){

        if (horarioDto != null) {
            Optional<HorarioExcepcion> horarioOptional = horarioRepository.findByUuid(horarioDto.getUuid());

            if (horarioOptional.isPresent()) {
                HorarioExcepcion horarioModificado = HorarioExcepcionMapper.toEntity(horarioDto);
                Optional<CampoDeportivo> cancha=canchasRepository.findByUuid(horarioDto.getCampoDeportivoDto().getUuid());
                if (cancha.isPresent())
                    horarioModificado.setIdHorarioExcepcion(horarioOptional.get().getIdHorarioExcepcion());
                    horarioModificado.setCampoDeportivo(cancha.get());


                return HorarioExcepcionMapper.toDto(horarioRepository.save(horarioModificado),true);
            } else {
                throw new NotFoundException(HORARIOEXCEPCION, horarioDto.getUuid());
            }
        } else {
            throw new NullReferenceException(HORARIOEXCEPCION);
        }
    }
    public String eliminarHorario(String uuid){

        HorarioExcepcion horario = horarioRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(HORARIOEXCEPCION, uuid));
        horarioRepository.delete(horario);
        return "Eliminacion exitosa";
    }
    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<HorarioExcepcion> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            HorarioExcepcion horario = horarioOptional.get();
//            HorarioFijo.setEstado(true);
//            HorarioFijoRepository.save(HorarioFijo);
            horarioRepository.delete(horario);

            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(HORARIOEXCEPCION, uuid);
        }
    }
}
