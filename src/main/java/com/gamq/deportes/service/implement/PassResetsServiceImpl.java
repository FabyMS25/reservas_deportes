package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.PassResetsDto;
import com.gamq.deportes.dto.mapper.PassResetsMapper;
import com.gamq.deportes.dto.mapper.PassResetsMapper;
import com.gamq.deportes.dto.mapper.PassResetsMapper;
import com.gamq.deportes.dto.mapper.PassResetsMapper;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.*;
import com.gamq.deportes.model.PassResets;
import com.gamq.deportes.repository.PassResetsRepository;
import com.gamq.deportes.service.PassResetsService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class PassResetsServiceImpl implements PassResetsService {

    PassResetsRepository passResetsRepository;


   public PassResetsDto findByUuid(String uuid){
       Optional<PassResets> PassOptional = passResetsRepository.findByUuid(uuid);
       if (PassOptional.isPresent()) {
           return PassResetsMapper.toDto(PassOptional.get());
       } else {
           throw new NotFoundException(PASSRESETS , uuid);
       }
   }
   public List<PassResetsDto> obtenerPassResets(){
       List<PassResets> PassResetss = passResetsRepository.findAll();

       if (!PassResetss.isEmpty()) {
           return PassResetss.stream().map(PassResets -> {
               return PassResetsMapper.toDto(PassResets);
           }).collect(Collectors.toList());
       } else {
           throw new NoResultException(PASSRESETS);
       }
   }
    public PassResetsDto crearPassResets(PassResetsDto PassResetsDto){
        if (PassResetsDto != null) {
            PassResets newPass = PassResetsMapper.toEntity(PassResetsDto);
            newPass =passResetsRepository.save(newPass);
            return PassResetsMapper.toDto(newPass);
        } else {
            throw new NullReferenceException(PASSRESETS);
        }
       
    }
    
    public PassResetsDto actualizarPassResets(PassResetsDto PassResetsDto){

        if (PassResetsDto != null) {
        Optional<PassResets> PassResetsOptional = passResetsRepository.findByUuid(PassResetsDto.getUuid());

        if (PassResetsOptional.isPresent()) {


            PassResets PassResetsModificado = PassResetsMapper.toEntity(PassResetsDto);
            PassResetsModificado.setIdPass(PassResetsOptional.get().getIdPass());

            return PassResetsMapper.toDto(passResetsRepository.save(PassResetsModificado));
        } else {
            throw new NotFoundException(PASSRESETS, PassResetsDto.getUuid());
        }
    } else {
        throw new NullReferenceException(PASSRESETS);
    }
    }
}
