package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.complejoDto;
import com.gamq.licencias.dto.mapper.CategoriaMapper;
import com.gamq.licencias.dto.mapper.complejoMapper;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;

import com.gamq.licencias.model.complejo;
import com.gamq.licencias.repository.complejoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.CATEGORIA;
import static com.gamq.licencias.exception.enums.EntityType.COMPLEJO;

@Component
public class complejoServiceImpl {
    @Autowired
    complejoRepository complejoRepository;

    public complejoDto findByUuid(String uuid){
        Optional<complejo> complejoOptional = complejoRepository.findByUuid(uuid);
        if (complejoOptional.isPresent()) {
            return complejoMapper.toDto(complejoOptional.get());
        } else {
            throw new NotFoundException(COMPLEJO , uuid);
        }


    }
    public List<complejoDto> obtenerComplejos(){
        List<complejo> complejos = complejoRepository.findAll();

        if (!complejos.isEmpty()) {
            return complejos.stream().map(comple -> {
                return complejoMapper.toDto(comple);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(COMPLEJO);
        }

    }
    public complejoDto crearComplejo(complejoDto complejoDto){

        if (complejoDto != null) {

            complejo newComplejo = complejoMapper.toEntity(complejoDto);
            newComplejo =complejoRepository.save(newComplejo);
            return complejoMapper.toDto(newComplejo);
        } else {
            throw new NullReferenceException(COMPLEJO);
        }
    }

    public complejoDto actualizarComplejo(complejoDto complejoDto){
        if (complejoDto != null) {
            Optional<complejo> complejoOptional = complejoRepository.findByUuid(complejoDto.getUuid());

            if (complejoOptional.isPresent()) {
                complejo complejoModificado = complejoMapper.toEntity(complejoDto);
                complejoModificado.setIdComplejo(complejoOptional.get().getIdComplejo());

                return complejoMapper.toDto(complejoRepository.save(complejoModificado));
            } else {
                throw new NotFoundException(COMPLEJO, complejoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(COMPLEJO);
        }
    }

}
