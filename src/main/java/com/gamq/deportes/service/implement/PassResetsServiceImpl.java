package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.mapper.PassMapper;
import com.gamq.deportes.dto.mapper.ContribuyenteMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Pass;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.repository.PassRepository;
import com.gamq.deportes.repository.ContribuyenteRepository;
import com.gamq.deportes.service.PassResetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class PassResetsServiceImpl implements PassResetsService {
    @Autowired
    PassRepository passRepository;
    @Autowired
    ContribuyenteRepository contribuyentesRepository;

   public PassDto findByUuid(String uuid){
       Optional<Pass> PassOptional = passRepository.findByUuid(uuid);
       if (PassOptional.isPresent()) {
           return PassMapper.toDto(PassOptional.get());
       } else {
           throw new NotFoundException(PASSRESETS , uuid);
       }
   }
    public PassDto findByToken(String token){
        Optional<Pass> PassOptional = passRepository.findByToken(token);
        if (PassOptional.isPresent()) {
            Pass passReset = PassOptional.get();
            // Check if the token has expired
            if (LocalDateTime.now().isAfter(passReset.getExpiresAt())) {
                throw new RuntimeException("El link ha expirado");
            }
            return PassMapper.toDto(passReset);
        } else {
            throw new NotFoundException(PASSRESETS ,token);
        }
    }
    public ContribuyenteDto findUserByToken(String token){
        Optional<Pass> PassOptional = passRepository.findByToken(token);
        if (PassOptional.isPresent()) {
            PassDto passDto= PassMapper.toDto(PassOptional.get());
            Optional<Contribuyente> contriOptional = contribuyentesRepository.findByEmail(passDto.getEmail());
            if (contriOptional.isPresent()) {
                return ContribuyenteMapper.toDto(contriOptional.get(),true);
            } else {
                throw new NotFoundException(CONTRIBUYENTE , passDto.getEmail());
            }

//            return PassMapper.toDto(PassOptional.get());
        } else {
            throw new NotFoundException(PASSRESETS ,token);
        }
    }
   public List<PassDto> obtenerPassResets(){
       List<Pass> passResetsses = passRepository.findAll();

       if (!passResetsses.isEmpty()) {
           return passResetsses.stream().map(Pass -> {
               return PassMapper.toDto(Pass);
           }).collect(Collectors.toList());
       } else {
           throw new NoResultException(PASSRESETS);
       }
   }
    public PassDto crearPassResets(PassDto PassDto){
        if (PassDto != null) {
            Pass newPass = PassMapper.toEntity(PassDto);
            newPass = passRepository.save(newPass);
            return PassMapper.toDto(newPass);
        } else {
            throw new NullReferenceException(PASSRESETS);
        }
       
    }
    
    public PassDto actualizarPassResets(PassDto PassDto){

        if (PassDto != null) {
        Optional<Pass> PassResetsOptional = passRepository.findByUuid(PassDto.getUuid());

        if (PassResetsOptional.isPresent()) {


            Pass passModificado = PassMapper.toEntity(PassDto);
            passModificado.setIdPass(PassResetsOptional.get().getIdPass());

            return PassMapper.toDto(passRepository.save(passModificado));
        } else {
            throw new NotFoundException(PASSRESETS, PassDto.getUuid());
        }
    } else {
        throw new NullReferenceException(PASSRESETS);
    }
    }
}
