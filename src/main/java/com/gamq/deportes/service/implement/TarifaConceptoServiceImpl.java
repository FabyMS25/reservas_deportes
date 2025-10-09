package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaConceptoDto;
import com.gamq.deportes.dto.mapper.TarifaConceptoMapper;
import com.gamq.deportes.model.TarifaConcepto;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.repository.TarifaConceptoRepository;
import com.gamq.deportes.repository.TarifaRepository;
import com.gamq.deportes.service.TarifaConceptoService;
import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class TarifaConceptoServiceImpl implements TarifaConceptoService {

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Override
    public TarifaConceptoDto findByUuid(String uuid) {
        Optional<TarifaConcepto> conceptoOptional = tarifaConceptoRepository.findByUuid(uuid);
        if (conceptoOptional.isPresent()) {
            return TarifaConceptoMapper.toDto(conceptoOptional.get());
        } else {
            throw new NotFoundException(TARIFA_CONCEPTO, uuid);
        }
    }

    @Override
    public List<TarifaConceptoDto> obtenerConceptos() {
        List<TarifaConcepto> conceptos = tarifaConceptoRepository.findAll();

        if (!conceptos.isEmpty()) {
            return conceptos.stream()
                    .map(TarifaConceptoMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA_CONCEPTO);
        }
    }

    @Override
    public List<TarifaConceptoDto> findConceptosByTarifa(String uuidTarifa) {
        List<TarifaConcepto> conceptos = tarifaConceptoRepository.findByTarifaUuid(uuidTarifa);

        if (!conceptos.isEmpty()) {
            return conceptos.stream()
                    .map(TarifaConceptoMapper::toDto)
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA_CONCEPTO);
        }
    }

    @Override
    @Transactional
    public TarifaConceptoDto crearConcepto(TarifaConceptoDto conceptoDto) {
        if (conceptoDto == null) {
            throw new NullReferenceException(TARIFA_CONCEPTO);
        }

        TarifaConcepto concepto = TarifaConceptoMapper.toEntity(conceptoDto);

        // Set tarifa relationship
        Tarifa tarifa = tarifaRepository.findByUuid(conceptoDto.getTarifaDto().getUuid())
                .orElseThrow(() -> new NotFoundException(TARIFA, conceptoDto.getTarifaDto().getUuid()));
        concepto.setTarifa(tarifa);

        TarifaConcepto conceptoGuardado = tarifaConceptoRepository.save(concepto);
        return TarifaConceptoMapper.toDto(conceptoGuardado);
    }

    @Override
    @Transactional
    public TarifaConceptoDto actualizarConcepto(TarifaConceptoDto conceptoDto) {
        if (conceptoDto == null) {
            throw new NullReferenceException(TARIFA_CONCEPTO);
        }

        TarifaConcepto conceptoExistente = tarifaConceptoRepository.findByUuid(conceptoDto.getUuid())
                .orElseThrow(() -> new NotFoundException(TARIFA_CONCEPTO, conceptoDto.getUuid()));

        // Update concepto information
        TarifaConceptoMapper.updateEntityFromDto(conceptoDto, conceptoExistente);

        // Update tarifa relationship if provided
        if (conceptoDto.getTarifaDto() != null) {
            Tarifa tarifa = tarifaRepository.findByUuid(conceptoDto.getTarifaDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, conceptoDto.getTarifaDto().getUuid()));
            conceptoExistente.setTarifa(tarifa);
        }

        TarifaConcepto conceptoActualizado = tarifaConceptoRepository.save(conceptoExistente);
        return TarifaConceptoMapper.toDto(conceptoActualizado);
    }

    @Override
    @Transactional
    public String eliminarConcepto(String uuid) {
        try {
            TarifaConcepto concepto = tarifaConceptoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(TARIFA_CONCEPTO, uuid));

            // Soft delete
            concepto.setEstado(true);
            tarifaConceptoRepository.save(concepto);
            return "Desactivación exitosa del concepto de tarifa";
        } catch (Exception e) {
            throw new RuntimeException("Error al intentar desactivar el concepto de tarifa.", e);
        }
    }

    @Override
    @Transactional
    public String eliminarConceptoPermanente(String uuid) {
        TarifaConcepto concepto = tarifaConceptoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(TARIFA_CONCEPTO, uuid));

        tarifaConceptoRepository.delete(concepto);
        return "Eliminación permanente exitosa del concepto de tarifa";
    }
}