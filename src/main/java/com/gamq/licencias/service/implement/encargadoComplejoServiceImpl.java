package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.TarifaMapper;
import com.gamq.licencias.dto.mapper.encargadoComplejoMapper;
import com.gamq.licencias.dto.encargadoComplejoDto;
import com.gamq.licencias.model.complejo;
import com.gamq.licencias.model.encargadoComplejo;
import com.gamq.licencias.model.horario;
import com.gamq.licencias.repository.complejoRepository;
import com.gamq.licencias.repository.encargadoComplejoRepository;
import com.gamq.licencias.service.encargadoComplejoService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;

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
    @Override
    public String delete(String uuid) {
        Optional<encargadoComplejo> encargadoOptional = encargadoComplejoRepository.findByUuid(uuid);
        if (encargadoOptional.isPresent()) {
            encargadoComplejo encargado = encargadoOptional.get();

            encargado.setEstado(true);
            encargadoComplejoRepository.save(encargado);

            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(ENCARGADOCOMPLEJO, uuid);
        }
    }
}
