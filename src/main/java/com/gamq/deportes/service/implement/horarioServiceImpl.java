package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.horarioMapper;
import com.gamq.deportes.dto.horarioDto;
import com.gamq.deportes.model.*;
import com.gamq.deportes.model.horario;
import com.gamq.deportes.repository.canchasRepository;
import com.gamq.deportes.repository.horarioRepository;
import com.gamq.deportes.service.horarioService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class horarioServiceImpl implements horarioService {
    
    @Autowired
    horarioRepository horarioRepository;

    @Autowired
    canchasRepository canchasRepository;
    
    public horarioDto findByUuid(String uuid){

        Optional<horario> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            return horarioMapper.toDTo(horarioOptional.get(), true);
        } else {
            throw new NotFoundException(HORARIO , uuid);
        }

    }
    public List<horarioDto> obtenerHorarios(){

        List<horario> horarios = horarioRepository.findAll();

        if (!horarios.isEmpty()) {
            return horarios.stream().map(horario -> {
                return horarioMapper.toDTo(horario,true);
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
            return horarioMapper.toDTo(horarioNew,true);
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


                return horarioMapper.toDTo(horarioRepository.save(horarioModificado),true);
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
    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<horario> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            horario horario = horarioOptional.get();
//            horario.setEstado(true);
//            horarioRepository.save(horario);
            horarioRepository.delete(horario);

            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(HORARIO, uuid);
        }
    }
}
