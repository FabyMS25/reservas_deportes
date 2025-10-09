package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.mapper.ContribuyenteMapper;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.service.ContribuyenteService;
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
public class ContribuyenteServiceImpl implements ContribuyenteService {


    @Autowired
    ContribuyenteRepository contribuyentesRepository;

    public ContribuyenteDto findByUuid(String uuid){

        Optional<Contribuyente> contriOptional = contribuyentesRepository.findByUuid(uuid);
        if (contriOptional.isPresent()) {
            return ContribuyenteMapper.toDto(contriOptional.get(),true);
        } else {
            throw new NotFoundException(CONTRIBUYENTE , uuid);
        }
    }
    public ContribuyenteDto findByEmail(String email){

        Optional<Contribuyente> contriOptional = contribuyentesRepository.findByEmail(email);
        if (contriOptional.isPresent()) {
            return ContribuyenteMapper.toDto(contriOptional.get(),true);
        } else {
            throw new NotFoundException(CONTRIBUYENTE , email);
        }
    }

    public List<ContribuyenteDto> obtenerContribuyentes(){
        List<Contribuyente> contribuyentes = contribuyentesRepository.findAll();

        if (!contribuyentes.isEmpty()) {
            return contribuyentes.stream().map(contri -> {
                return ContribuyenteMapper.toDto(contri,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CONTRIBUYENTE);
        }
    }

    public ContribuyenteDto crearContribuyente(ContribuyenteDto contribuyenteDto){

        if (contribuyenteDto != null) {

            Contribuyente newContri     = ContribuyenteMapper.toEntity(contribuyenteDto);
            return ContribuyenteMapper.toDto(contribuyentesRepository.save(newContri),true);
        } else {
            throw new NullReferenceException(CONTRIBUYENTE);
        }
    }

    public ContribuyenteDto actualizarContribuyente(ContribuyenteDto contribuyenteDto){
        if (contribuyenteDto != null) {
            Optional<Contribuyente> contribuyenteOptional = contribuyentesRepository.findByUuid(contribuyenteDto.getUuid());

            if (contribuyenteOptional.isPresent()) {
                Contribuyente contribuyenteModificado = ContribuyenteMapper.toEntity(contribuyenteDto);
                contribuyenteModificado.setIdContribuyente(contribuyenteOptional.get().getIdContribuyente());

                return ContribuyenteMapper.toDto(contribuyentesRepository.save(contribuyenteModificado),true);
            } else {
                throw new NotFoundException(CANCHAS, contribuyenteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
        
    }
    public String eliminarContribuyente(String uuid){

        Contribuyente contribuyentes = contribuyentesRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONTRIBUYENTE, uuid));
        contribuyentesRepository.delete(contribuyentes);

        return "Eliminacion exitosa";
    }
    public ContribuyenteDto loginContribuyente(login login){

        if ((login.getUser()!= null)&&(login.getPass()!= null)) {
            Optional<Contribuyente> contribuyenteOptional = contribuyentesRepository.Login(login.getUser(), login.getPass());

                return ContribuyenteMapper.toDto(contribuyenteOptional.get(),true);
        } else {
                throw new NotFoundException(CONTRIBUYENTE, login.getUser());

        }
    }
    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<Contribuyente> found = contribuyentesRepository.findByUuid(uuid);
        if (found.isPresent()) {
            Contribuyente contribuyente = found.get();
//            HorarioFijo.setEstado(true);
//            HorarioFijoRepository.save(HorarioFijo);
            contribuyentesRepository.delete(contribuyente);

            return "Eliminacion exitosa";
        } else {
            throw new NotFoundException(CONTRIBUYENTE, uuid);
        }
    }


}
