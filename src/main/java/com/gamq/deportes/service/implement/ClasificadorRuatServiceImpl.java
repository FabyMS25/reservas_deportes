package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ClasificadorRuatDto;
import com.gamq.deportes.dto.mapper.ClasificadorRuatMapper;
import com.gamq.deportes.model.ClasificadorRuat;
import com.gamq.deportes.repository.ClasificadorRuatRepository;
import com.gamq.deportes.service.ClasificadorRuatService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Service
@Transactional
public class ClasificadorRuatServiceImpl implements ClasificadorRuatService {

    @Autowired
    private ClasificadorRuatRepository clasificadorRuatRepository;

    @Override
    public ClasificadorRuatDto findByUuid(String uuid) {
        Optional<ClasificadorRuat> clasificadorOptional = clasificadorRuatRepository.findByUuid(uuid);
        if (clasificadorOptional.isPresent()) {
            return ClasificadorRuatMapper.toDto(clasificadorOptional.get());
        } else {
            throw new NotFoundException(CLASIFICADOR_RUAT, uuid);
        }
    }

    @Override
    public ClasificadorRuatDto findByCodigo(String codigo) {
        Optional<ClasificadorRuat> clasificadorOptional = clasificadorRuatRepository.findByCodigo(codigo);
        if (clasificadorOptional.isPresent()) {
            return ClasificadorRuatMapper.toDto(clasificadorOptional.get());
        } else {
            throw new NotFoundException(CLASIFICADOR_RUAT, codigo);
        }
    }

    @Override
    public List<ClasificadorRuatDto> obtenerClasificadores() {
        List<ClasificadorRuat> clasificadores = clasificadorRuatRepository.findAll();

        return clasificadores.stream()
                .map(ClasificadorRuatMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClasificadorRuatDto> obtenerClasificadoresActivos() {
        List<ClasificadorRuat> clasificadores = clasificadorRuatRepository.findByActivoTrue();

        return clasificadores.stream()
                .map(ClasificadorRuatMapper::toDto)
                .collect(Collectors.toList());
    }

//    @Override
//    public List<ClasificadorRuatDto> obtenerClasificadoresPorTipoReserva(String tipoReserva) {
//        List<ClasificadorRuat> clasificadores = clasificadorRuatRepository.findByTipoReserva(tipoReserva);
//
//        if (!clasificadores.isEmpty()) {
//            return clasificadores.stream()
//                    .map(ClasificadorRuatMapper::toDto)
//                    .collect(Collectors.toList());
//        } else {
//            throw new NoResultException(CLASIFICADOR_RUAT);
//        }
//    }

    @Override
    @Transactional
    public ClasificadorRuatDto crearClasificador(ClasificadorRuatDto clasificadorDto) {
        if (clasificadorDto == null) {
            throw new NullReferenceException(CLASIFICADOR_RUAT);
        }

        // Check if codigo already exists
        if (clasificadorRuatRepository.findByCodigo(clasificadorDto.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un clasificador con el código: " + clasificadorDto.getCodigo());
        }

        ClasificadorRuat clasificador = ClasificadorRuatMapper.toEntity(clasificadorDto);
        ClasificadorRuat clasificadorGuardado = clasificadorRuatRepository.save(clasificador);
        return ClasificadorRuatMapper.toDto(clasificadorGuardado);
    }

    @Override
    @Transactional
    public ClasificadorRuatDto actualizarClasificador(ClasificadorRuatDto clasificadorDto) {
        if (clasificadorDto == null) {
            throw new NullReferenceException(CLASIFICADOR_RUAT);
        }

        ClasificadorRuat clasificadorExistente = clasificadorRuatRepository.findByUuid(clasificadorDto.getUuid())
                .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, clasificadorDto.getUuid()));

        // Only check for duplicate if the code is actually being changed
        if (!clasificadorExistente.getCodigo().equals(clasificadorDto.getCodigo())) {
            Optional<ClasificadorRuat> existingWithSameCode = clasificadorRuatRepository.findByCodigo(clasificadorDto.getCodigo());
            if (existingWithSameCode.isPresent()) {
                throw new RuntimeException("Ya existe un clasificador con el código: " + clasificadorDto.getCodigo());
            }
        }

        // Update clasificador information
        ClasificadorRuatMapper.updateEntityFromDto(clasificadorDto, clasificadorExistente);

        try {
            ClasificadorRuat clasificadorActualizado = clasificadorRuatRepository.save(clasificadorExistente);
            return ClasificadorRuatMapper.toDto(clasificadorActualizado);
        } catch (DataIntegrityViolationException ex) {
            // Fallback: if our duplicate check failed, catch the database exception
            if (ex.getMessage().contains("codigo") || ex.getMessage().contains("unique") || ex.getMessage().contains("duplicate")) {
                throw new RuntimeException("Ya existe un clasificador con el código: " + clasificadorDto.getCodigo());
            }
            throw ex;
        }
    }

    @Override
    @Transactional
    public String eliminarClasificador(String uuid) {
        try {
            ClasificadorRuat clasificador = clasificadorRuatRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, uuid));

            // Check if clasificador is used in any tarifa
            if (!clasificador.getTarifasList().isEmpty()) {
                throw new RuntimeException("No se puede eliminar el clasificador porque está siendo utilizado en tarifas existentes.");
            }

            // Soft delete
            clasificador.setEstado(true);
            clasificadorRuatRepository.save(clasificador);
            return "Desactivación exitosa del clasificador RUAT";
        } catch (Exception e) {
            throw new RuntimeException("Error al intentar desactivar el clasificador RUAT.", e);
        }
    }

    @Override
    @Transactional
    public String eliminarClasificadorPermanente(String uuid) {
        ClasificadorRuat clasificador = clasificadorRuatRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, uuid));

        // Check if clasificador is used in any tarifa
        if (!clasificador.getTarifasList().isEmpty()) {
            throw new RuntimeException("No se puede eliminar permanentemente el clasificador porque está siendo utilizado en tarifas existentes.");
        }

        clasificadorRuatRepository.delete(clasificador);
        return "Eliminación permanente exitosa del clasificador RUAT";
    }
}