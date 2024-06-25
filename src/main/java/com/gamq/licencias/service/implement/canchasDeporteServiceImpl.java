package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.canchasDeporteMapper;

import com.gamq.licencias.dto.canchasDeporteDto;
import com.gamq.licencias.model.canchaDeportes;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.deportes;
import com.gamq.licencias.repository.canchaDeportesRepository;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.repository.deportesRepository;
import com.gamq.licencias.service.canchasDeporteService;
import com.gamq.licencias.exception.NoResultException;
import com.gamq.licencias.exception.NotFoundException;
import com.gamq.licencias.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.gamq.licencias.exception.enums.EntityType.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.licencias.exception.enums.EntityType.*;


@Component
public class canchasDeporteServiceImpl implements canchasDeporteService {
    @Autowired
    canchaDeportesRepository canchaDeportesRepository;

    @Autowired
    canchasRepository canchasRepository;

    @Autowired
    deportesRepository deportesRepository;

    public canchasDeporteDto findByUuid(String uuid){

        Optional<canchaDeportes> canchasDeporteOptional = canchaDeportesRepository.findByUuid(uuid);
        if (canchasDeporteOptional.isPresent()) {
            return canchasDeporteMapper.toDto(canchasDeporteOptional.get());
        } else {
            throw new NotFoundException(CANCHASDEPORTES , uuid);
        }

    }
    public List<canchasDeporteDto> obtenerCanchasDeporte(){

        List<canchaDeportes> canchaDeportes = canchaDeportesRepository.findAll();

        if (!canchaDeportes.isEmpty()) {
            return canchaDeportes.stream().map(canchaDepor -> {
                return canchasDeporteMapper.toDto(canchaDepor);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHASDEPORTES);
        }
    }
    public canchasDeporteDto crearCanchaDeporte(canchasDeporteDto canchasDeporteDto){

        if (canchasDeporteDto != null) {
            canchas cancha = getCancha(canchasDeporteDto.getCanchasDto().getUuid());
            deportes deporte= getDeporte(canchasDeporteDto.getDeporteDto().getUuid());
            canchaDeportes newCanchaDepor = canchasDeporteMapper.toEntity(canchasDeporteDto);
            newCanchaDepor.setCancha(cancha);
            newCanchaDepor.setDeporte(deporte);
            return canchasDeporteMapper.toDto(canchaDeportesRepository.save(newCanchaDepor));
        } else {
            throw new NullReferenceException(CANCHASDEPORTES);
        }
    }
    public canchasDeporteDto actualizarCanchaDeporte(canchasDeporteDto canchasDeporteDto){

        if (canchasDeporteDto != null) {
            Optional<canchaDeportes> canchaDeportesOptional = canchaDeportesRepository.findByUuid(canchasDeporteDto.getUuid());

            if (canchaDeportesOptional.isPresent()) {
                canchas cancha = getCancha(canchasDeporteDto.getCanchasDto().getUuid());
                deportes deporte= getDeporte(canchasDeporteDto.getDeporteDto().getUuid());
                canchaDeportes canchaDeportesModificado = canchasDeporteMapper.toEntity(canchasDeporteDto);
                canchaDeportesModificado.setIdCanchaDeporte(canchaDeportesOptional.get().getIdCanchaDeporte());
                canchaDeportesModificado.setCancha(cancha);
                canchaDeportesModificado.setDeporte(deporte);
                return canchasDeporteMapper.toDto(canchaDeportesRepository.save(canchaDeportesModificado));
            } else {
                throw new NotFoundException(CANCHASDEPORTES, canchasDeporteDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHASDEPORTES);
        }
    }
    public String eliminarCanchasDeporte(String uuid){

        return null;
    }
    private canchas getCancha(String Uuid) {
        return canchasRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, Uuid));
    }
    private deportes getDeporte(String Uuid) {
        return deportesRepository.findByUuid(Uuid)
                .orElseThrow(() -> new NotFoundException(DEPORTES, Uuid));
    }

}
