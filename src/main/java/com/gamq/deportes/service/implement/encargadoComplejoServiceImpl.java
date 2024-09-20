package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.encargadoComplejoMapper;
import com.gamq.deportes.dto.encargadoComplejoDto;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.complejoRepository;
import com.gamq.deportes.repository.encargadoComplejoRepository;
import com.gamq.deportes.service.encargadoComplejoService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class encargadoComplejoServiceImpl implements encargadoComplejoService {

    @Autowired
    encargadoComplejoRepository encargadoComplejoRepository;

    @Autowired
    complejoRepository complejoRepository;


    public encargadoComplejoDto findByUuid(String uuid){

        Optional<encargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUuid(uuid);
        if (ENCARGADOCOMPLEJOOptional.isPresent()) {
            return encargadoComplejoMapper.toDto(ENCARGADOCOMPLEJOOptional.get(),true);
        } else {
            throw new NotFoundException(ENCARGADOCOMPLEJO , uuid);
        }

    }

    public encargadoComplejoDto findByUserUuid(String userUuid){
        Optional<encargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUserUuid(userUuid);
        if (ENCARGADOCOMPLEJOOptional.isPresent()) {
            return encargadoComplejoMapper.toDto(ENCARGADOCOMPLEJOOptional.get(),true);
        } else {
            throw new NotFoundException(ENCARGADOCOMPLEJO , userUuid);
        }
    }
    public List<encargadoComplejoDto> obtenerEncargadosComplejos(){

        List<encargadoComplejo> encargadoComplejos = encargadoComplejoRepository.findAll();

        if (!encargadoComplejos.isEmpty()) {
            return encargadoComplejos.stream().map(ENCARGADOCOMPLEJO -> {
                return encargadoComplejoMapper.toDto(ENCARGADOCOMPLEJO,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(ENCARGADOCOMPLEJO);
        }
    }

    public encargadoComplejoDto crearEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            complejo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());
            encargadoComplejo newEncargadoComplejo = encargadoComplejoMapper.toEntity(encargadoComplejoDto);
            newEncargadoComplejo.setComplejo(complejo);
            return encargadoComplejoMapper.toDto(encargadoComplejoRepository.save(newEncargadoComplejo),true);
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }

    public encargadoComplejoDto actualizarEncargadoComplejo(encargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            Optional<encargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUuid(encargadoComplejoDto.getUuid());

            if (ENCARGADOCOMPLEJOOptional.isPresent()) {

                complejo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());

                encargadoComplejo encargadoComplejoModificado = encargadoComplejoMapper.toEntity(encargadoComplejoDto);
                encargadoComplejoModificado.setIdEncargadoComplejo(ENCARGADOCOMPLEJOOptional.get().getIdEncargadoComplejo());

                encargadoComplejoModificado.setComplejo(complejo);
                return encargadoComplejoMapper.toDto(encargadoComplejoRepository.save(encargadoComplejoModificado),true);
            } else {
                throw new NotFoundException(ENCARGADOCOMPLEJO, encargadoComplejoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(ENCARGADOCOMPLEJO);
        }
    }
   /* @Override
    public String eliminarENCARGADOCOMPLEJO(String uuid){

        return null;
    }*/

    private complejo getComplejo(String Uuid) {
        return complejoRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(COMPLEJO, Uuid));
    }
    @Transactional
    public String delete(String uuid) {
        try {
            encargadoComplejo encargado = encargadoComplejoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(ENCARGADOCOMPLEJO, uuid));
            // Perform soft delete
            encargado.setEstado(true);
            encargadoComplejoRepository.save(encargado);

            return "Desactivación exitosa del ENCARGADO";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el registro. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }
}
