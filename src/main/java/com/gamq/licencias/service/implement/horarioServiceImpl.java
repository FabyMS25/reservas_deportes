package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.*;
import com.gamq.licencias.dto.mapper.horarioMapper;
import com.gamq.licencias.dto.mapper.horarioMapper;
import com.gamq.licencias.dto.mapper.horarioMapper;
import com.gamq.licencias.dto.horarioDto;
import com.gamq.licencias.model.*;
import com.gamq.licencias.model.horario;
import com.gamq.licencias.model.horario;
import com.gamq.licencias.model.horario;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.repository.horarioRepository;
import com.gamq.licencias.service.horarioService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.CANCHAS;
import static com.gamq.licencias.exception.enums.EntityType.HORARIO;

@Component
public class horarioServiceImpl implements horarioService {
    
    @Autowired
    horarioRepository horarioRepository;

    @Autowired
    canchasRepository canchasRepository;
    
    public horarioDto findByUuid(String uuid){

        Optional<horario> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            return horarioMapper.toDTo(horarioOptional.get());
        } else {
            throw new NotFoundException(HORARIO , uuid);
        }

    }
    public List<horarioDto> obtenerHorarios(){

        List<horario> horarios = horarioRepository.findAll();

        if (!horarios.isEmpty()) {
            return horarios.stream().map(horario -> {
                return horarioMapper.toDTo(horario);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(HORARIO);
        }
    }

    public horarioDto crearHorario(horarioDto horarioDto){

        if (horarioDto != null) {

            horario newHorario = horarioMapper.toEntity(horarioDto);
            Optional<canchas> cancha=canchasRepository.findByUuid(horarioDto.getCanchasDto().getUuid());
            if (cancha.isPresent())
                newHorario.setCancha(cancha.get());
                horario horarioNew= horarioRepository.save(newHorario);
            return horarioMapper.toDTo(horarioNew);
        } else {
            throw new NullReferenceException(HORARIO);
        }
    }
    public horarioDto actualizarHorario(horarioDto horarioDto){

        if (horarioDto != null) {
            Optional<horario> horarioOptional = horarioRepository.findByUuid(horarioDto.getUuid());

            if (horarioOptional.isPresent()) {
                horario horarioModificado = horarioMapper.toEntity(horarioDto);
                horarioModificado.setIdHorario(horarioOptional.get().getIdHorario());
                Optional<canchas> cancha=canchasRepository.findByUuid(horarioDto.getCanchasDto().getUuid());
                if (cancha.isPresent())
                    horarioModificado.setCancha(cancha.get());


                return horarioMapper.toDTo(horarioRepository.save(horarioModificado));
            } else {
                throw new NotFoundException(HORARIO, horarioDto.getUuid());
            }
        } else {
            throw new NullReferenceException(HORARIO);
        }
    }
    public String eliminarHorario(String uuid){

        horario horario = horarioRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(HORARIO, uuid));
        horarioRepository.delete(horario);
        return "Eliminacion exitosa";
    }
}
