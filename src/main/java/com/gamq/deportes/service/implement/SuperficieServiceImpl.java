package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.SuperficieDto;
import com.gamq.deportes.dto.mapper.SuperficieMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.model.Superficie;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.repository.SuperficieRepository;
import com.gamq.deportes.repository.CampoDeportivoRepository;

import com.gamq.deportes.service.SuperficieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.SUPERFICIE;

@Component
public class SuperficieServiceImpl implements SuperficieService {
    @Autowired
    SuperficieRepository superficieRepository;

    @Autowired
    CampoDeportivoRepository canchaRepository;

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

        return Superficies.stream().map(Superfi -> {
            return SuperficieMapper.toDto(Superfi);
        }).collect(Collectors.toList());

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
    @Transactional
    @Override
    public String delete(String uuid) {
        Optional<Superficie> item = superficieRepository.findByUuid(uuid);
        if (item.isPresent()) {
            Superficie surface = item.get();

            List<CampoDeportivo> canchasAsociadas = canchaRepository.findBySuperficieUuid(surface.getUuid());
            if (!canchasAsociadas.isEmpty()) {
                return "No se pudo eliminar la SUPERFICIE porque existen campoDeportivo asociadas a esta misma";
            }
//            // Soft delete associated campoDeportivo
//            for (campoDeportivo cancha : canchasAsociadas) {
//                canchaRepository.delete(cancha);
//            }

            // (soft delete)
//            surface.setEstado(true);
            superficieRepository.delete(surface);

            return "Eliminación exitosa de superficie";
        } else {
            throw new NotFoundException(SUPERFICIE, uuid); // Handle the not-found case
        }
    }

}
