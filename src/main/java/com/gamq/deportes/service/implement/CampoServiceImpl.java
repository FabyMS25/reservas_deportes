package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.mapper.CampoDeportivoMapper;
import com.gamq.deportes.dto.CampoDeportivoDto;
import com.gamq.deportes.model.*;

import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.CampoService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;


@Component
public class CampoServiceImpl implements CampoService {

    @Autowired
    CampoDeportivoRepository canchasRepository;

    @Autowired
    ComplejoDeportivoRepository complejoRepository;

    @Autowired
    SuperficieRepository superficieRepository;

    public CampoDeportivoDto findByUuid(String uuid){
        Optional<CampoDeportivo> canchasOptional = canchasRepository.findByUuid(uuid);
        if (canchasOptional.isPresent()) {
            return CampoDeportivoMapper.toDto(canchasOptional.get(),true);
        } else {
            throw new NotFoundException(CANCHAS , uuid);
        }
    }

    public List<CampoDeportivoDto> findCampoByComplejo(String uuidComplejo){
        List<CampoDeportivo> canchass = canchasRepository.findByComplejoUuid(uuidComplejo);

        if (!canchass.isEmpty()) {
            return canchass.stream().map(canchas -> {
                return CampoDeportivoMapper.toDto(canchas,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHAS);
        }
    }
    public List<CampoDeportivoDto> obtenerCampos(){
        List<CampoDeportivo> canchass = canchasRepository.findAll();

        if (!canchass.isEmpty()) {
            return canchass.stream().map(canchas -> {
                return CampoDeportivoMapper.toDto(canchas,true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHAS);
        }

    }

    public CampoDeportivoDto crearCampo(CampoDeportivoDto campoDto) {
        if (campoDto != null) {
            CampoDeportivo nuevoCampo = CampoDeportivoMapper.toEntity(campoDto);
            ComplejoDeportivo complejo= obtenerComplejo(campoDto.getComplejoDto().getUuid());
            Superficie superficie= obtenerSuperficie(campoDto.getSuperficieDto().getUuid());

            nuevoCampo.setComplejo(complejo);
            nuevoCampo.setSuperficie(superficie);

            CampoDeportivo campoGuardado = canchasRepository.save(nuevoCampo);
            return CampoDeportivoMapper.toDto(campoGuardado, true);
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }

//    @Override
//    public CampoDeportivoDto actualizarCampo(CampoDeportivoDto campoDto) {
//        if (campoDto != null) {
//            CampoDeportivo campoExistente = canchasRepository.findByUuid(campoDto.getUuid())
//                    .orElseThrow(() -> new NotFoundException(CANCHAS, campoDto.getUuid()));
//            // Update basic fields
//            CampoDeportivoMapper.updateEntityFromDto(campoDto, campoExistente);
//            // Update relationships if provided
//            if (campoDto.getComplejoId() != null) {
//                ComplejoDeportivo complejo = obtenerComplejo(campoDto.getComplejoId());
//                campoExistente.setComplejo(complejo);
//            }
//            if (campoDto.getSuperficieId() != null) {
//                Superficie superficie = obtenerSuperficie(campoDto.getSuperficieId());
//                campoExistente.setSuperficie(superficie);
//            }
//
//            CampoDeportivo campoActualizado = canchasRepository.save(campoExistente);
//            return CampoDeportivoMapper.toDto(campoActualizado, true);
//        } else {
//            throw new NullReferenceException(CANCHAS);
//        }
//    }

    public CampoDeportivoDto actualizarCampo(CampoDeportivoDto campoDto){
        if (campoDto != null) {
           Optional<CampoDeportivo> canchasOptional = canchasRepository.findByUuid(campoDto.getUuid());
            if (canchasOptional.isPresent()) {
                ComplejoDeportivo complejo= obtenerComplejo(campoDto.getComplejoDto().getUuid());
                Superficie superficie= obtenerSuperficie(campoDto.getSuperficieDto().getUuid());
                CampoDeportivo canchasModificado = CampoDeportivoMapper.toEntity(campoDto);
                canchasModificado.setComplejo(complejo);
                canchasModificado.setSuperficie(superficie);
                return CampoDeportivoMapper.toDto(canchasRepository.save(canchasModificado),true);
            } else {
                throw new NotFoundException(CANCHAS, campoDto.getUuid());
            }
        } else {
            throw new NullReferenceException(CANCHAS);
        }
    }

    public ComplejoDeportivo obtenerComplejo(String uuid ){
            return complejoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(COMPLEJO, uuid));
    }
    public Superficie obtenerSuperficie(String uuid){
        return superficieRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(SUPERFICIE, uuid));
    }

    @Transactional
    public String eliminarCampo(String uuid) {
        try {
            CampoDeportivo campo = canchasRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
            // Perform soft delete
            campo.setEstado(true);
            for (Tarifa tarifa : campo.getTarifasList()) {
                if (!tarifa.getCategoriaDtoList().isEmpty()) {
                    for (TarifaCategoria tarifaCat : new ArrayList<>(tarifa.getCategoriaDtoList())) {
                        if (tarifaCat != null && tarifaCat.getUuid() != null) {
                            tarifaCat.setEstado(true);
                        }
                    }
                    tarifa.getCategoriaDtoList().clear();
                }
                tarifa.setEstado(true);
            }
            for (HorarioFijo horario : campo.getHorariosFijosList()) {
                horario.setEstado(true);
            }
            for (HorarioExcepcion horario : campo.getHorariosExcepcionesList()) {
                horario.setEstado(true);
            }
    //            campo.getTarifasList().clear();
    //            cancha.getHorarioList().clear();
            canchasRepository.save(campo);

            return "Desactivación exitosa de CANCHA y sus relaciones asociadas";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar la CANCHA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }

    public String eliminarCampos(String uuid){
        CampoDeportivo canchas = canchasRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
        canchasRepository.delete(canchas);

        return "Eliminacion exitosa";

    }
}
