package com.gamq.licencias.service.implement;

import com.gamq.licencias.dto.mapper.canchasMapper;
import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.complejo;
import com.gamq.licencias.model.Superficie;

import com.gamq.licencias.repository.SuperficieRepository;
import com.gamq.licencias.repository.canchasRepository;
import com.gamq.licencias.repository.complejoRepository;
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

    @Autowired
    complejoRepository complejoRepository;

    @Autowired
    SuperficieRepository superficieRepository;

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
            complejo comple= obtenerComplejo(canchasDto.complejoDto.getUuid());
            Superficie superficie= obtenerSuperficie(canchasDto.getSuperficieDto().getUuid());
            newCancha.setComplejo(comple);
            newCancha.setSuperficie(superficie);
            /*newCancha.setTarifasList(null);
            newCancha.setReservasList(null);
            newCancha.setHorarioList(null);*/
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

                complejo comple= obtenerComplejo(canchasDto.complejoDto.getUuid());
                Superficie superficie= obtenerSuperficie(canchasDto.getSuperficieDto().getUuid());
                canchas canchasModificado = canchasMapper.toEntity(canchasDto);
                canchasModificado.setIdCancha(canchasOptional.get().getIdCancha());
                canchasModificado.setComplejo(comple);
                canchasModificado.setSuperficie(superficie);
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
    public complejo obtenerComplejo(String uuid ){
            return complejoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(COMPLEJO, uuid));
    }
    public Superficie obtenerSuperficie(String uuid){
        return superficieRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(SUPERFICIE, uuid));
    }
}
