package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ComplejoDeportivoDto;
import com.gamq.deportes.model.ComplejoDeportivo;
import com.gamq.deportes.model.CampoDeportivo;
import com.gamq.deportes.repository.ComplejoDeportivoRepository;
import com.gamq.deportes.service.ComplejoService;
import com.gamq.deportes.dto.mapper.ComplejoDeportivoMapper;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.repository.CampoDeportivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplejoServiceImpl implements ComplejoService {

    @Autowired
    private ComplejoDeportivoRepository complejoRepository;
    @Autowired
    private CampoDeportivoRepository campoDeportivoRepository;
    @Override
    public ComplejoDeportivoDto findByUuid(String uuid) {
        Optional<ComplejoDeportivo> complejoOptional = complejoRepository.findByUuid(uuid);
        if (complejoOptional.isPresent()) {
            return ComplejoDeportivoMapper.toDto(complejoOptional.get());
        } else {
            throw new NotFoundException(COMPLEJO, uuid);
        }
    }

    @Override
    public List<ComplejoDeportivoDto> obtenerComplejos() {
        List<ComplejoDeportivo> complejos = complejoRepository.findAll();

        return complejos.stream()
                .map(ComplejoDeportivoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ComplejoDeportivoDto crearComplejo(ComplejoDeportivoDto complejoDto) {
        if (complejoDto != null) {
            ComplejoDeportivo newComplejo = ComplejoDeportivoMapper.toEntity(complejoDto);
            newComplejo = complejoRepository.save(newComplejo);
            return ComplejoDeportivoMapper.toDto(newComplejo);
        } else {
            throw new NullReferenceException(COMPLEJO);
        }
    }

    @Override
    public ComplejoDeportivoDto actualizarComplejo(ComplejoDeportivoDto complejoDto) {
        if (complejoDto != null) {
            ComplejoDeportivo complejoExistente = complejoRepository.findByUuid(complejoDto.getUuid())
                    .orElseThrow(() -> new NotFoundException(COMPLEJO, complejoDto.getUuid()));

            ComplejoDeportivoMapper.updateEntityFromDto(complejoDto, complejoExistente);
            ComplejoDeportivo complejoActualizado = complejoRepository.save(complejoExistente);
            return ComplejoDeportivoMapper.toDto(complejoActualizado);
        } else {
            throw new NullReferenceException(COMPLEJO);
        }
    }

    @Override
    @Transactional
    public String eliminarComplejo(String uuid) {
        try {
            ComplejoDeportivo complejo = complejoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(COMPLEJO, uuid));

            // Check if there are active campos deportivos
            List<CampoDeportivo> camposAsociados = campoDeportivoRepository.findByComplejoUuid(uuid);
            boolean hasActiveCampos = camposAsociados.stream()
                    .anyMatch(campo -> !campo.isEstado());

            if (hasActiveCampos) {
                return "No se pudo eliminar el COMPLEJO porque existen campos deportivos activos asociados";
            }

            // Soft delete the complejo
            complejo.setEstado(true);
            complejoRepository.save(complejo);

            return "Desactivación exitosa del COMPLEJO";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar el COMPLEJO. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }
}