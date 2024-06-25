package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.ConstruccionDto;
import com.gamq.licencias.dto.mapper.ConstruccionMapper;

;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.exception.enums.EntityType;
import com.gamq.licencias.model.Construccion;

import com.gamq.licencias.repository.ConstruccionRepository;

import com.gamq.licencias.service.ConstruccionService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;

@Component
public class ConstruccionServiceImpl implements ConstruccionService {



    @Autowired
    ConstruccionRepository construccionRepository;



    public ConstruccionDto findByUuid(String uuid){
        Optional<Construccion> construccionOptional = construccionRepository.findByUuid(uuid);
        if (construccionOptional.isPresent()) {
           return ConstruccionMapper.ToDto(construccionOptional.get());
        } else {
            throw new NotFoundException(CONSTRUCCION, uuid);
        }

    }

    @Override
    public List<ConstruccionDto> obtenerConstrucciones() {
        List<Construccion> construccions = construccionRepository.findAll();

        if (!construccions.isEmpty()) {
            return construccions.stream().map(construccion -> {
                return ConstruccionMapper.ToDto(construccion);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CONSTRUCCION);
        }
    }

    @Override
    public ConstruccionDto crearConstruccion(ConstruccionDto construccionDto) {
        if (construccionDto != null) {

            Construccion newCons = ConstruccionMapper.ToEntity(construccionDto);

         return ConstruccionMapper.ToDto(construccionRepository.save(newCons));
        } else {
            throw new NullReferenceException(CONSTRUCCION);
        }
    }

    @Override
    public ConstruccionDto actualizarConstruccion(ConstruccionDto construccionDto) {
        if (construccionDto != null) {

//            ActividadEconomica actividadEconomicaQBE = new ActividadEconomica(actividadEconomicaDto.getUuid());
//            Optional<ActividadEconomica> actividadOptional = actividadRepository.findOne(Example.of(actividadEconomicaQBE));
            Optional<Construccion> construccionOptional = construccionRepository.findByUuid(construccionDto.getUuid());

            if (construccionOptional.isPresent()) {
                Construccion consModificado = ConstruccionMapper.ToEntity(construccionDto);



                return ConstruccionMapper.ToDto(construccionRepository.save(consModificado));
            } else {
                throw new NotFoundException(CONSTRUCCION, construccionDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CONSTRUCCION);
        }
    }

    @Override
    public String eliminarConstruccion(String uuid) {
        Construccion construccion = construccionRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CONSTRUCCION, uuid));
        construccionRepository.delete(construccion);

        return "Eliminacion exitosa";
    }


}
