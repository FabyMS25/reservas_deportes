package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaConceptoDto;
import com.gamq.deportes.dto.mapper.TarifaConceptoMapper;
import com.gamq.deportes.model.TarifaConcepto;
import com.gamq.deportes.model.Tarifa;
import com.gamq.deportes.repository.TarifaConceptoRepository;
import com.gamq.deportes.repository.TarifaRepository;
import com.gamq.deportes.service.SistemaAuditoriaService;
import com.gamq.deportes.service.TarifaConceptoService;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TarifaConceptoServiceImpl implements TarifaConceptoService {

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private SistemaAuditoriaService sistemaAuditoriaService;

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

        return conceptos.stream()
                .map(TarifaConceptoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TarifaConceptoDto> findConceptosByTarifa(String uuidTarifa) {
        List<TarifaConcepto> conceptos = tarifaConceptoRepository.findByTarifaUuid(uuidTarifa);

        return conceptos.stream()
                .map(TarifaConceptoMapper::toDto)
                .collect(Collectors.toList());
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
        registrarAuditoriaSistema(
                "CREAR_TARIFA_CONCEPTO",
                conceptoGuardado,
                "Concepto de tarifa creado"
        );
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
        String datosAnterior = buildTarifaConceptoSnapshot(conceptoExistente);

        // Update concepto information
        TarifaConceptoMapper.updateEntityFromDto(conceptoDto, conceptoExistente);

        // Update tarifa relationship if provided
        if (conceptoDto.getTarifaDto() != null) {
            Tarifa tarifa = tarifaRepository.findByUuid(conceptoDto.getTarifaDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, conceptoDto.getTarifaDto().getUuid()));
            conceptoExistente.setTarifa(tarifa);
        }

        TarifaConcepto conceptoActualizado = tarifaConceptoRepository.save(conceptoExistente);
        sistemaAuditoriaService.registrarAccion(
                null,
                "ACTUALIZAR_TARIFA_CONCEPTO",
                "TARIFAS",
                "tarifa_concepto",
                conceptoActualizado.getUuid(),
                "Concepto de tarifa actualizado",
                "{\"anterior\":" + datosAnterior + ",\"nuevo\":" + buildTarifaConceptoSnapshot(conceptoActualizado) + "}",
                null,
                null
        );
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
            registrarAuditoriaSistema(
                    "ELIMINAR_TARIFA_CONCEPTO",
                    concepto,
                    "Concepto de tarifa desactivado"
            );
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
        registrarAuditoriaSistema(
                "ELIMINAR_TARIFA_CONCEPTO",
                concepto,
                "Concepto de tarifa eliminado permanentemente"
        );
        return "Eliminación permanente exitosa del concepto de tarifa";
    }

    private void registrarAuditoriaSistema(String accion, TarifaConcepto concepto, String descripcion) {
        sistemaAuditoriaService.registrarAccion(
                null,
                accion,
                "TARIFAS",
                "tarifa_concepto",
                concepto.getUuid(),
                descripcion,
                buildTarifaConceptoSnapshot(concepto),
                null,
                null
        );
    }

    private String buildTarifaConceptoSnapshot(TarifaConcepto concepto) {
        return "{\"uuid\":\"" + safeValue(concepto.getUuid())
                + "\",\"tarifaUuid\":\"" + safeValue(concepto.getTarifa() != null ? concepto.getTarifa().getUuid() : null)
                + "\",\"concepto\":\"" + safeValue(concepto.getConcepto())
                + "\",\"tipo\":\"" + safeValue(concepto.getTipo())
                + "\",\"valor\":\"" + safeValue(concepto.getValor())
                + "\",\"desde\":\"" + safeValue(concepto.getDesde())
                + "\",\"hasta\":\"" + safeValue(concepto.getHasta())
                + "\",\"activo\":\"" + safeValue(concepto.getActivo())
                + "\",\"estado\":\"" + concepto.isEstado() + "\"}";
    }

    private String safeValue(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }
}
