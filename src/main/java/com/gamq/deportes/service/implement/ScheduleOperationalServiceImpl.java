package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.HorarioFijoMapper;
import com.gamq.deportes.dto.HorarioFijoDto;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.model.HorarioFijo;
import com.gamq.deportes.repository.CampoDeportivoRepository;
import com.gamq.deportes.repository.HorarioFijoRepository;
import com.gamq.deportes.service.ScheduleOperationalService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ScheduleOperationalServiceImpl implements ScheduleOperationalService {
    
    @Autowired
    HorarioFijoRepository horarioRepository;

    @Autowired
    CampoDeportivoRepository canchasRepository;
    
    public HorarioFijoDto findByUuid(String uuid){

        Optional<HorarioFijo> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            return HorarioFijoMapper.toDto(horarioOptional.get(), true);
        } else {
            throw new NotFoundException(HORARIOFIJO , uuid);
        }

    }
    public List<HorarioFijoDto> obtenerHorarios(){

        List<HorarioFijo> horarios = horarioRepository.findAll();

        return horarios.stream().map(horario -> {
            return HorarioFijoMapper.toDto(horario,true);
        }).collect(Collectors.toList());
    }
    @Override
    public HorarioFijoDto crearHorario(HorarioFijoDto horarioDto){
        if (horarioDto != null) {
            HorarioFijo newHorario = HorarioFijoMapper.toEntity(horarioDto);
            Optional<CampoDeportivo> cancha=canchasRepository.findByUuid(horarioDto.getCampoDeportivoDto().getUuid());
            if (cancha.isPresent())
                newHorario.setCampoDeportivo(cancha.get());
                HorarioFijo horarioNew= horarioRepository.save(newHorario);
            return HorarioFijoMapper.toDto(horarioNew,true);
        } else {
            throw new NullReferenceException(HORARIOFIJO);
        }
    }

    public HorarioFijoDto actualizarHorario(HorarioFijoDto horarioDto){
        if (horarioDto != null) {
            Optional<HorarioFijo> horarioOptional = horarioRepository.findByUuid(horarioDto.getUuid());
            if (horarioOptional.isPresent()) {
                CampoDeportivo campo = canchasRepository.findByUuid(horarioDto.getCampoDeportivoDto().getUuid())
                        .orElseThrow(() -> new NotFoundException(COMPLEJO, horarioDto.getCampoDeportivoDto().getUuid()));
                HorarioFijo horarioModificado = HorarioFijoMapper.toEntity(horarioDto);
                horarioModificado.setIdHorarioFijo(horarioOptional.get().getIdHorarioFijo());
//                Optional<CampoDeportivo> cancha=canchasRepository.findByUuid(horarioDto.getCampoDeportivoDto().getUuid());
//                if (cancha.isPresent())
                horarioModificado.setCampoDeportivo(campo);
                return HorarioFijoMapper.toDto(horarioRepository.save(horarioModificado),true);
            } else {
                throw new NotFoundException(HORARIOFIJO, horarioDto.getUuid());
            }
        } else {
            throw new NullReferenceException(HORARIOFIJO);
        }
    }
    public String eliminarHorario(String uuid){

        HorarioFijo horario = horarioRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(HORARIOFIJO, uuid));
        horarioRepository.delete(horario);
        return "Eliminacion exitosa";
    }
    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<HorarioFijo> horarioOptional = horarioRepository.findByUuid(uuid);
        if (horarioOptional.isPresent()) {
            HorarioFijo horario = horarioOptional.get();
//            HorarioFijo.setEstado(true);
//            HorarioFijoRepository.save(HorarioFijo);
            horarioRepository.delete(horario);

            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(HORARIOFIJO, uuid);
        }
    }
}

