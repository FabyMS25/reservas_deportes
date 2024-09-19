package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.complejoDto;
import com.gamq.licencias.model.Superficie;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.service.complejoService;

import com.gamq.licencias.dto.mapper.CategoriaMapper;
import com.gamq.licencias.dto.mapper.complejoMapper;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;

import com.gamq.licencias.model.complejo;
import com.gamq.licencias.repository.complejoRepository;
import com.gamq.licencias.repository.canchasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;

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
