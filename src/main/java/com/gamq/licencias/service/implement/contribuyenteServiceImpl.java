package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.contribuyentesMapper;
import com.gamq.licencias.dto.contribuyenteDto;
import com.gamq.licencias.model.contribuyentes;
import com.gamq.licencias.repository.contribuyentesRepository;
import com.gamq.licencias.service.contribuyenteService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;


@Component
public class contribuyenteServiceImpl implements contribuyenteService {


    @Autowired
    contribuyentesRepository contribuyentesRepository;

    public contribuyenteDto findByUuid(String uuid){

        Optional<contribuyentes> contriOptional = contribuyentesRepository.findByUuid(uuid);
        if (contriOptional.isPresent()) {
            return contribuyentesMapper.toDto(contriOptional.get());
        } else {
            throw new NotFoundException(CONTRIBUYENTE , uuid);
        }
    }
    public List<contribuyenteDto> obtenerContribuyentes(){
        List<contribuyentes> contribuyentes = contribuyentesRepository.findAll();

        if (!contribuyentes.isEmpty()) {
            return contribuyentes.stream().map(contri -> {
                return contribuyentesMapper.toDto(contri);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CONTRIBUYENTE);
        }
    }

    public contribuyenteDto crearContribuyente(contribuyenteDto contribuyenteDto){

        if (contribuyenteDto != null) {

            contribuyentes newContri     = contribuyentesMapper.toEntity(contribuyenteDto);
            return contribuyentesMapper.toDto(contribuyentesRepository.save(newContri));
        } else {
            throw new NullReferenceException(CONTRIBUYENTE);
        }
    }

    public contribuyenteDto actualizarContribuyente(contribuyenteDto contribuyenteDto){
        if (contribuyenteDto != null) {
            Optional<contribuyentes> contribuyenteOptional = contribuyentesRepository.findByUuid(contribuyenteDto.getUuid());

            if (contribuyenteOptional.isPresent()) {
                contribuyentes contribuyenteModificado = contribuyentesMapper.toEntity(contribuyenteDto);
                contribuyenteModificado.setIdContribuyente(contribuyenteOptional.get().getIdContribuyente());

                return contribuyentesMapper.toDto(contribuyentesRepository.save(contribuyenteModificado));
            } else {
                throw new NotFoundException(CANCHAS, contribuyenteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
        
    }
    public String eliminarContribuyente(String uuid){

        contribuyentes contribuyentes = contribuyentesRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, uuid));
        contribuyentesRepository.delete(contribuyentes);

        return "Eliminacion exitosa";
    }
    public contribuyenteDto loginContribuyente(String user, String pass){

        if ((user != null)&&(pass!= null)) {
            Optional<contribuyentes> contribuyenteOptional = contribuyentesRepository.Login(user, pass);

                return contribuyentesMapper.toDto(contribuyenteOptional.get());
        } else {
                throw new NotFoundException(CONTRIBUYENTE, user);
        }

    }
}
