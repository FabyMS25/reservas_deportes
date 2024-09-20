package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.complejoDto;
import com.gamq.deportes.model.canchas;
import com.gamq.deportes.service.complejoService;

import com.gamq.deportes.dto.mapper.complejoMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;

import com.gamq.deportes.model.complejo;
import com.gamq.deportes.repository.complejoRepository;
import com.gamq.deportes.repository.canchasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class complejoServiceImpl implements complejoService {
    @Autowired
    complejoRepository complejoRepository;

    @Autowired
    canchasRepository canchasRepository;

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

    @Transactional
    @Override
    public String deleteComplex(String uuid) {
        try {
            complejo complejo = complejoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
            // Perform soft delete
            List<canchas> canchasAsociadas = canchasRepository.findCanchasByComplejo(complejo.getUuid());
            if (!canchasAsociadas.isEmpty()) {
                return "No se pudo eliminar el COMPLEJO porque existen canchas asociadas a esta misma";
            }

            complejoRepository.delete(complejo);

            return "Desactivación exitosa del COMPLEJO";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el COMPLEJO. Por favor, inténtelo de nuevo más tarde.", e);
        }

    }
}
