package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.canchasMapper;
import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.service.canchasService;
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
public class canchaServiceImpl implements canchasService {

@Autowired
    canchasRepository canchasRepository;

    public canchasDto findByUuid(String uuid){
        Optional<canchas> canchasOptional = canchasRepository.findByUuid(uuid);
        if (canchasOptional.isPresent()) {
            return canchasMapper.toDto(canchasOptional.get());
        } else {
            throw new NotFoundException(CANCHAS , uuid);
        }


    }
    public List<canchasDto> obtenerCanchas(){
        List<canchas> canchass = canchasRepository.findAll();

        if (!canchass.isEmpty()) {
            return canchass.stream().map(canchas -> {
                return canchasMapper.toDto(canchas);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHAS);
        }

    }
    public canchasDto crearCancha(canchasDto canchasDto){

        if (canchasDto != null) {

            canchas newCancha = canchasMapper.toEntity(canchasDto);
            newCancha =canchasRepository.save(newCancha);
            return canchasMapper.toDto(newCancha);
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }

    public canchasDto actualizarCancha(canchasDto canchasDto){
        if (canchasDto != null) {
           Optional<canchas> canchasOptional = canchasRepository.findByUuid(canchasDto.getUuid());

            if (canchasOptional.isPresent()) {
                canchas canchasModificado = canchasMapper.toEntity(canchasDto);
                canchasModificado.setIdCancha(canchasOptional.get().getIdCancha());

                return canchasMapper.toDto(canchasRepository.save(canchasModificado));
            } else {
                throw new NotFoundException(CANCHAS, canchasDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }
    public String eliminarCanchas(String uuid){
        canchas canchas = canchasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
        canchasRepository.delete(canchas);

        return "Eliminacion exitosa";

    }
}
