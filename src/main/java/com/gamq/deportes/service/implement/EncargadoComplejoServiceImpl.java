package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.EncargadoComplejoMapper;
import com.gamq.deportes.dto.EncargadoComplejoDto;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.ComplejoDeportivoRepository;
import com.gamq.deportes.repository.EncargadoComplejoRepository;
import com.gamq.deportes.service.EncargadoComplejoService;
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
public class EncargadoComplejoServiceImpl implements EncargadoComplejoService {

    @Autowired
    EncargadoComplejoRepository encargadoComplejoRepository;

    @Autowired
    ComplejoDeportivoRepository complejoRepository;


    public EncargadoComplejoDto findByUuid(String uuid){

        Optional<EncargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUuid(uuid);
        if (ENCARGADOCOMPLEJOOptional.isPresent()) {
            return EncargadoComplejoMapper.toDto(ENCARGADOCOMPLEJOOptional.get(),true);
        } else {
            throw new NotFoundException(ENCARGADOCOMPLEJO , uuid);
        }

    }

    public EncargadoComplejoDto findByUserUuid(String userUuid){
        Optional<EncargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUserUuid(userUuid);
        if (ENCARGADOCOMPLEJOOptional.isPresent()) {
            return EncargadoComplejoMapper.toDto(ENCARGADOCOMPLEJOOptional.get(),true);
        } else {
            throw new NotFoundException(ENCARGADOCOMPLEJO , userUuid);
        }
    }
    public List<EncargadoComplejoDto> obtenerEncargadosComplejos(){

        List<EncargadoComplejo> encargadoComplejos = encargadoComplejoRepository.findAll();

        if (!encargadoComplejos.isEmpty()) {
            return encargadoComplejos.stream().map(ENCARGADOCOMPLEJO -> {
                return EncargadoComplejoMapper.toDto(ENCARGADOCOMPLEJO,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(ENCARGADOCOMPLEJO);
        }
    }

    public EncargadoComplejoDto crearEncargadoComplejo(EncargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            ComplejoDeportivo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());
            EncargadoComplejo newEncargadoComplejo = EncargadoComplejoMapper.toEntity(encargadoComplejoDto);
            newEncargadoComplejo.setComplejo(complejo);
            return EncargadoComplejoMapper.toDto(encargadoComplejoRepository.save(newEncargadoComplejo),true);
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }

    public EncargadoComplejoDto actualizarEncargadoComplejo(EncargadoComplejoDto encargadoComplejoDto){

        if (encargadoComplejoDto != null) {
            Optional<EncargadoComplejo> ENCARGADOCOMPLEJOOptional = encargadoComplejoRepository.findByUuid(encargadoComplejoDto.getUuid());

            if (ENCARGADOCOMPLEJOOptional.isPresent()) {

                ComplejoDeportivo complejo = getComplejo(encargadoComplejoDto.getComplejoDto().getUuid());

                EncargadoComplejo encargadoComplejoModificado = EncargadoComplejoMapper.toEntity(encargadoComplejoDto);
                encargadoComplejoModificado.setIdEncargadoComplejo(ENCARGADOCOMPLEJOOptional.get().getIdEncargadoComplejo());

                encargadoComplejoModificado.setComplejo(complejo);
                return EncargadoComplejoMapper.toDto(encargadoComplejoRepository.save(encargadoComplejoModificado),true);
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

    private ComplejoDeportivo getComplejo(String Uuid) {
        return complejoRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(COMPLEJO, Uuid));
    }
    @Transactional
    public String delete(String uuid) {
        try {
            EncargadoComplejo encargado = encargadoComplejoRepository.findByUuid(uuid)
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
