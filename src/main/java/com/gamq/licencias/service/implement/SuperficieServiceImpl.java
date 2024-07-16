package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.SuperficieDto;
import com.gamq.licencias.dto.mapper.SuperficieMapper;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.Superficie;
import com.gamq.licencias.repository.SuperficieRepository;

import com.gamq.licencias.service.SuperficieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.SUPERFICIE;


@Component
public class SuperficieServiceImpl implements SuperficieService {
    @Autowired
    SuperficieRepository superficieRepository;

    public SuperficieDto findByUuid(String uuid){
        Optional<Superficie> SuperficieOptional = superficieRepository.findByUuid(uuid);
        if (SuperficieOptional.isPresent()) {
            return SuperficieMapper.toDto(SuperficieOptional.get());
        } else {
            throw new NotFoundException(SUPERFICIE, uuid);
        }


    }
    public List<SuperficieDto> obtenerSuperficies(){
        List<Superficie> Superficies = superficieRepository.findAll();

        if (!Superficies.isEmpty()) {
            return Superficies.stream().map(Superfi -> {
                return SuperficieMapper.toDto(Superfi);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(SUPERFICIE);
        }

    }
    public SuperficieDto crearSuperficie(SuperficieDto SuperficieDto){

        if (SuperficieDto != null) {

            Superficie newSuperficie = SuperficieMapper.toEntity(SuperficieDto);
            newSuperficie =superficieRepository.save(newSuperficie);
            return SuperficieMapper.toDto(newSuperficie);
        } else {
            throw new NullReferenceException(SUPERFICIE);
        }
    }

    public SuperficieDto actualizarSuperficie(SuperficieDto SuperficieDto){
        if (SuperficieDto != null) {
            Optional<Superficie> SuperficieOptional = superficieRepository.findByUuid(SuperficieDto.getUuid());

            if (SuperficieOptional.isPresent()) {
                Superficie SuperficieModificado = SuperficieMapper.toEntity(SuperficieDto);
                SuperficieModificado.setIdSuperficie(SuperficieOptional.get().getIdSuperficie());

                return SuperficieMapper.toDto(superficieRepository.save(SuperficieModificado));
            } else {
                throw new NotFoundException(SUPERFICIE, SuperficieDto.getUuid());
            }
        } else {
            throw new NullReferenceException(SUPERFICIE);
        }
    }

}
