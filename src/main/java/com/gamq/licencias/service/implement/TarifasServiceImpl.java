package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.dto.mapper.TarifaMapper;

import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.Tarifas;
import com.gamq.licencias.repository.TarifasRepository;
import com.gamq.licencias.repository.TarifasRepository;
import com.gamq.licencias.service.TarifasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.TARIFA;


@Component
public class TarifasServiceImpl implements TarifasService {
    @Autowired 
    TarifasRepository tarifasRepository;

    public TarifaDto findByUuid(String uuid){
        Optional<Tarifas> TarifasOptional = tarifasRepository.findByUuid(uuid);
        if (TarifasOptional.isPresent()) {
            return TarifaMapper.toDto(TarifasOptional.get());
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }


    }
    public List<TarifaDto> obtenerTarifas(){
        List<Tarifas> Tarifass = tarifasRepository.findAll();

        if (!Tarifass.isEmpty()) {
            return Tarifass.stream().map(tarifa -> {
                return TarifaMapper.toDto(tarifa);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA);
        }

    }
    public TarifaDto crearTarifa(TarifaDto TarifaDto){

        if (TarifaDto != null) {

            Tarifas newTarifas = TarifaMapper.toEntity(TarifaDto);
            newTarifas =tarifasRepository.save(newTarifas);
            return TarifaMapper.toDto(newTarifas);
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

    public TarifaDto actualizarTarifa(TarifaDto TarifaDto){
        if (TarifaDto != null) {
            Optional<Tarifas> TarifasOptional = tarifasRepository.findByUuid(TarifaDto.getUuid());

            if (TarifasOptional.isPresent()) {
                Tarifas TarifasModificado = TarifaMapper.toEntity(TarifaDto);
                TarifasModificado.setIdTarifa(TarifasOptional.get().getIdTarifa());

                return TarifaMapper.toDto(tarifasRepository.save(TarifasModificado));
            } else {
                throw new NotFoundException(TARIFA, TarifaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

}
