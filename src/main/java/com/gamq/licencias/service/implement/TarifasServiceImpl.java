package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.TarifaDto;
import com.gamq.licencias.dto.mapper.TarifaMapper;

import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import com.gamq.licencias.model.Tarifas;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.repository.TarifasRepository;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.service.TarifasService;
import com.gamq.licencias.service.canchasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;


@Component
public class TarifasServiceImpl implements TarifasService {
    @Autowired 
    TarifasRepository tarifasRepository;

    @Autowired
    canchasRepository canchasRepository;

    public TarifaDto findByUuid(String uuid){
        Optional<Tarifas> TarifasOptional = tarifasRepository.findByUuid(uuid);
        if (TarifasOptional.isPresent()) {
            return TarifaMapper.toDto(TarifasOptional.get(),true);
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }


    }
    public List<TarifaDto> obtenerTarifas(){
        List<Tarifas> Tarifass = tarifasRepository.findAll();

        if (!Tarifass.isEmpty()) {
            return Tarifass.stream().map(tarifa -> {
                return TarifaMapper.toDto(tarifa, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA);
        }

    }
    public TarifaDto crearTarifa(TarifaDto TarifaDto){

        if (TarifaDto != null) {

            Tarifas newTarifas = TarifaMapper.toEntity(TarifaDto);
            canchas cancha = obtenerCancha(TarifaDto.getCanchasDto().getUuid());
            newTarifas.setCancha(cancha);
            newTarifas =tarifasRepository.save(newTarifas);
            return TarifaMapper.toDto(newTarifas,true);
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
                canchas cancha = obtenerCancha(TarifaDto.getCanchasDto().getUuid());
                TarifasModificado.setCancha(cancha);


                return TarifaMapper.toDto(tarifasRepository.save(TarifasModificado),true);
            } else {
                throw new NotFoundException(TARIFA, TarifaDto.getUuid());
            }
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

    public canchas obtenerCancha(String uuid){
        return canchasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }
}
