package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.deporteMapper;
import com.gamq.licencias.dto.mapper.deporteMapper;
import com.gamq.licencias.dto.mapper.deporteMapper;
import com.gamq.licencias.dto.mapper.deporteMapper;

import com.gamq.licencias.dto.deporteDto;

import com.gamq.licencias.model.deportes;
import com.gamq.licencias.repository.deportesRepository;
import com.gamq.licencias.service.deportesService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.CANCHAS;
import static com.gamq.licencias.exception.enums.EntityType.DEPORTES;
@Component
public class deportesServiceImpl implements deportesService {
    @Autowired
    deportesRepository deportesRepository;
    
    public deporteDto findByUuid(String uuid){

        Optional<deportes> deportesOptional = deportesRepository.findByUuid(uuid);
        if (deportesOptional.isPresent()) {
            return deporteMapper.toDto(deportesOptional.get());
        } else {
            throw new NotFoundException(DEPORTES , uuid);
        }

    }
    public List<deporteDto> obtenerDeportes(){

        List<deportes> deportes = deportesRepository.findAll();

        if (!deportes.isEmpty()) {
            return deportes.stream().map(deporte -> {
                return deporteMapper.toDto(deporte);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(DEPORTES);
        }
    }
    public deporteDto crearDeporte(deporteDto deporteDto){

        if (deporteDto != null) {

            deportes newDeporte = deporteMapper.toEntity(deporteDto);
            return deporteMapper.toDto(deportesRepository.save(newDeporte));
        } else {
            throw new NullReferenceException(DEPORTES);
        }
    }
    public deporteDto actualizarDeporte(deporteDto deporteDto){

        if (deporteDto != null) {
            Optional<deportes> deporteOptional = deportesRepository.findByUuid(deporteDto.getUuid());

            if (deporteOptional.isPresent()) {
                deportes deporteModificado = deporteMapper.toEntity(deporteDto);
                deporteModificado.setIdDeporte(deporteOptional.get().getIdDeporte());

                return deporteMapper.toDto(deportesRepository.save(deporteModificado));
            } else {
                throw new NotFoundException(DEPORTES, deporteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(DEPORTES);
        }
    }
    public String eliminarDeporte(String uuid){

        deportes deporte = deportesRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(DEPORTES, uuid));
        deportesRepository.delete(deporte);

        return "Eliminacion exitosa";
    }
}
