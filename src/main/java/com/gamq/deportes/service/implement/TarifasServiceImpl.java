package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.TarifaConceptoDto;
import com.gamq.deportes.dto.TarifaDto;
import com.gamq.deportes.dto.mapper.TarifaConceptoMapper;
import com.gamq.deportes.dto.mapper.TarifaMapper;

import com.gamq.deportes.exception.NoResultException;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.exception.NullReferenceException;
import com.gamq.deportes.exception.OperationNotAllowedException;
import com.gamq.deportes.model.*;
import com.gamq.deportes.repository.*;
import com.gamq.deportes.service.SistemaAuditoriaService;
import com.gamq.deportes.service.TarifasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class TarifasServiceImpl implements TarifasService {

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaCategoriaRepository tarifaCategoriaRepository;

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Autowired
    private CampoDeportivoRepository campoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClasificadorRuatRepository clasificadorRuatRepository;

    @Autowired
    private ReservaRepository reservaRepository; // Add this repository

    @Autowired
    private SistemaAuditoriaService sistemaAuditoriaService;

    public TarifaDto findByUuid(String uuid){
        Optional<Tarifa> TarifasOptional = tarifaRepository.findByUuid(uuid);
        if (TarifasOptional.isPresent()) {
            return TarifaMapper.toDto(TarifasOptional.get(),true);
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }
    }

    public List<TarifaDto> obtenerTarifas(){
        List<Tarifa> Tarifass = tarifaRepository.findAll();
        if (!Tarifass.isEmpty()) {
            return Tarifass.stream().map(tarifa -> {
                return TarifaMapper.toDto(tarifa, true);
            }).collect(Collectors.toList());
        } else {
            throw new NoResultException(TARIFA);
        }
    }

    public TarifaDto crearTarifa(TarifaDto tarifaDto){
        if (tarifaDto != null) {
            Tarifa newTarifa = TarifaMapper.toEntity(tarifaDto);
            CampoDeportivo campo = obtenerCampo(tarifaDto.getCampoDeportivoDto().getUuid());
            newTarifa.setCampoDeportivo(campo);
            ClasificadorRuat clasificador = obtenerClasificadorRuat(tarifaDto.getClasificadorRuatDto().getUuid());
            newTarifa.setClasificadorRuat(clasificador);

            Tarifa tarifaGuardada = tarifaRepository.save(newTarifa);

            if (tarifaDto.getCategoriasDtoList() != null && !tarifaDto.getCategoriasDtoList().isEmpty()) {
                crearTarifaCategorias(tarifaGuardada, tarifaDto.getCategoriasDtoList());
            }
            if (tarifaDto.getConceptosDtos() != null && !tarifaDto.getConceptosDtos().isEmpty()) {
                crearTarifaConceptos(tarifaGuardada, tarifaDto.getConceptosDtos());
            }
            Tarifa tarifaCompleta = tarifaRepository.findByUuid(tarifaGuardada.getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, tarifaGuardada.getUuid()));

            registrarAuditoriaSistema(
                    "CREAR_TARIFA",
                    "tarifa",
                    tarifaCompleta.getUuid(),
                    "Tarifa creada",
                    buildTarifaSnapshot(tarifaCompleta)
            );
            return TarifaMapper.toDto(tarifaCompleta, true);
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

    @Transactional
    public TarifaDto actualizarTarifa(TarifaDto tarifaDto) {
        if (tarifaDto != null) {
            Tarifa tarifaExistente  = tarifaRepository.findByUuid(tarifaDto.getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, tarifaDto.getUuid()));
            String datosAnterior = buildTarifaSnapshot(tarifaExistente);

            // Check if tariff is already soft deleted
            if (tarifaExistente.isEstado()) {
                throw new OperationNotAllowedException("No se puede actualizar una tarifa que ha sido eliminada");
            }

            TarifaMapper.updateEntityFromDto(tarifaDto, tarifaExistente);

            if (tarifaDto.getCampoDeportivoDto() != null) {
                CampoDeportivo campo = obtenerCampo(tarifaDto.getCampoDeportivoDto().getUuid());
                tarifaExistente.setCampoDeportivo(campo);
            }

            if (tarifaDto.getClasificadorRuatDto() != null) {
                ClasificadorRuat clasificador = obtenerClasificadorRuat(tarifaDto.getClasificadorRuatDto().getUuid());
                tarifaExistente.setClasificadorRuat(clasificador);
            }

            Tarifa tarifaActualizada = tarifaRepository.save(tarifaExistente);

            if (tarifaDto.getCategoriasDtoList() != null) {
                actualizarTarifaCategorias(tarifaActualizada, tarifaDto.getCategoriasDtoList());
            }

            if (tarifaDto.getConceptosDtos() != null) {
                actualizarTarifaConceptos(tarifaActualizada, tarifaDto.getConceptosDtos());
            }

            Tarifa tarifaCompleta = tarifaRepository.findByUuid(tarifaActualizada.getUuid())
                    .orElseThrow(() -> new NotFoundException(TARIFA, tarifaActualizada.getUuid()));

            registrarAuditoriaSistema(
                    "ACTUALIZAR_TARIFA",
                    "tarifa",
                    tarifaCompleta.getUuid(),
                    "Tarifa actualizada",
                    "{\"anterior\":" + datosAnterior + ",\"nuevo\":" + buildTarifaSnapshot(tarifaCompleta) + "}"
            );
            return TarifaMapper.toDto(tarifaCompleta, true);
        } else {
            throw new NullReferenceException(TARIFA);
        }
    }

    @Transactional
    @Override
    public String deleteTarifa(String uuid) {
        Optional<Tarifa> tarifaOptional = tarifaRepository.findByUuid(uuid);
        if (tarifaOptional.isPresent()) {
            Tarifa tarifa = tarifaOptional.get();

            if (tarifa.isEstado()) {
                throw new OperationNotAllowedException("La tarifa ya ha sido eliminada");
            }

            if (isTarifaUsedInReservas(tarifa)) {
                throw new OperationNotAllowedException(
                        "No se puede eliminar la tarifa porque está siendo utilizada en una o más reservas activas"
                );
            }

            if (!tarifa.getCategoriasList().isEmpty()) {
                for (TarifaCategoria tarifaCat : tarifa.getCategoriasList()) {
                    if (tarifaCat != null) {
                        tarifaCat.setEstado(true); // Soft delete
                        tarifaCategoriaRepository.save(tarifaCat);
                    }
                }
            }

            if (!tarifa.getConceptos().isEmpty()) {
                for (TarifaConcepto concepto : tarifa.getConceptos()) {
                    if (concepto != null) {
                        concepto.setEstado(true); // Soft delete
                        tarifaConceptoRepository.save(concepto);
                    }
                }
            }

            tarifa.setEstado(true);
            tarifaRepository.save(tarifa);
            registrarAuditoriaSistema(
                    "ELIMINAR_TARIFA",
                    "tarifa",
                    tarifa.getUuid(),
                    "Tarifa eliminada",
                    buildTarifaSnapshot(tarifa)
            );

            return "Eliminación exitosa (soft delete)";
        } else {
            throw new NotFoundException(TARIFA, uuid);
        }
    }

    /**
     * Check if a tariff is being used in any active reservations
     */
    private boolean isTarifaUsedInReservas(Tarifa tarifa) {
        // Assuming you have a method in reservaRepository to check for active reservations using this tariff
        List<Reserva> activeReservas = reservaRepository.findActiveReservasByTarifa(tarifa.getIdTarifa());

        // Alternative approach if you don't have that method:
        // List<Reserva> allReservas = reservaRepository.findAll();
        // return allReservas.stream()
        //     .anyMatch(reserva ->
        //         reserva.getTarifaDto() != null &&
        //         reserva.getTarifaDto().getUuid().equals(tarifa.getUuid()) &&
        //         !reserva.isEstado() && // Active reservation
        //         !"RECHAZADA".equals(reserva.getEstadoValidacion()) // Not rejected
        //     );

        return !activeReservas.isEmpty();
    }

    private CategoriaDeportiva obtenerCategoria(String uuid) {
        return categoriaRepository.findByUuid(uuid)
                .orElseThrow(() -> new EntityNotFoundException("CategoriaDeportiva not found with UUID: " + uuid));
    }

    public CampoDeportivo obtenerCampo(String uuid){
        return campoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
    }

    private ClasificadorRuat obtenerClasificadorRuat(String uuid) {
        return clasificadorRuatRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, uuid));
    }

    private void crearTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> categoriasDto) {
        for (TarifaCategoriaDto tarifaCatDto : categoriasDto) {
            TarifaCategoria tarifaCategoria = new TarifaCategoria();
            tarifaCategoria.setTarifa(tarifa);
            tarifaCategoria.setEstado(false); // Active by default

            CategoriaDeportiva categoria = categoriaRepository.findByUuid(tarifaCatDto.getCategoriaDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(CATEGORIA, tarifaCatDto.getCategoriaDto().getUuid()));
            tarifaCategoria.setCategoria(categoria);

            tarifaCategoriaRepository.save(tarifaCategoria);
        }
    }

    private void actualizarTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> categoriasDto) {
        // Soft delete existing categorias
        if (tarifa.getCategoriasList() != null) {
            for (TarifaCategoria tarifaCat : tarifa.getCategoriasList()) {
                tarifaCat.setEstado(true);
                tarifaCategoriaRepository.save(tarifaCat);
            }
        }

        // Create new categorias
        crearTarifaCategorias(tarifa, categoriasDto);
    }

    private void actualizarTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> conceptosDto) {
        // Soft delete existing conceptos
        if (tarifa.getConceptos() != null) {
            for (TarifaConcepto concepto : tarifa.getConceptos()) {
                concepto.setEstado(true);
                tarifaConceptoRepository.save(concepto);
                registrarAuditoriaSistema(
                        "ELIMINAR_TARIFA_CONCEPTO",
                        "tarifa_concepto",
                        concepto.getUuid(),
                        "Concepto de tarifa reemplazado durante actualización de tarifa",
                        buildTarifaConceptoSnapshot(concepto)
                );
            }
        }

        // Create new conceptos
        crearTarifaConceptos(tarifa, conceptosDto);
    }

    private void crearTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> conceptosDto) {
        for (TarifaConceptoDto conceptoDto : conceptosDto) {
            TarifaConcepto concepto = TarifaConceptoMapper.toEntity(conceptoDto);
            concepto.setTarifa(tarifa);
            concepto.setEstado(false); // Active by default
            TarifaConcepto conceptoGuardado = tarifaConceptoRepository.save(concepto);
            registrarAuditoriaSistema(
                    "CREAR_TARIFA_CONCEPTO",
                    "tarifa_concepto",
                    conceptoGuardado.getUuid(),
                    "Concepto de tarifa creado",
                    buildTarifaConceptoSnapshot(conceptoGuardado)
            );
        }
    }

    private void registrarAuditoriaSistema(String accion, String entidadTipo, String entidadUuid,
                                           String descripcion, String datosCambio) {
        sistemaAuditoriaService.registrarAccion(
                null,
                accion,
                "TARIFAS",
                entidadTipo,
                entidadUuid,
                descripcion,
                datosCambio,
                null,
                null
        );
    }

    private String buildTarifaSnapshot(Tarifa tarifa) {
        return "{\"uuid\":\"" + safeValue(tarifa.getUuid())
                + "\",\"precioBase\":\"" + safeValue(tarifa.getPrecioBase())
                + "\",\"tipoCobro\":\"" + safeValue(tarifa.getTipoCobro())
                + "\",\"frecuencia\":\"" + safeValue(tarifa.getFrecuencia())
                + "\",\"fechaInicio\":\"" + safeValue(tarifa.getFechaInicio())
                + "\",\"fechaFin\":\"" + safeValue(tarifa.getFechaFin())
                + "\",\"activo\":\"" + safeValue(tarifa.getActivo())
                + "\",\"estado\":\"" + tarifa.isEstado() + "\"}";
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
