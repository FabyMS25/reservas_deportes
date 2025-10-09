package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.*;
import com.gamq.deportes.dto.mapper.*;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.gamq.deportes.exception.enums.EntityType.*;

@Component
public class CampoServiceImpl implements CampoService {

    @Autowired
    CampoDeportivoRepository campoRepository;
    @Autowired
    ComplejoDeportivoRepository complejoRepository;
    @Autowired
    SuperficieRepository superficieRepository;

    @Autowired
    private TarifaRepository tarifaRepository;

    @Autowired
    private TarifaCategoriaRepository tarifaCategoriaRepository;

    @Autowired
    private TarifaConceptoRepository tarifaConceptoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ClasificadorRuatRepository clasificadorRuatRepository;

    @Autowired
    private HorarioFijoRepository horarioFijoRepository;

    @Autowired
    private HorarioExcepcionRepository horarioExcepcionRepository;

    public CampoDeportivoDto findByUuid(String uuid){
        Optional<CampoDeportivo> campoOptional = campoRepository.findByUuid(uuid);
        if (campoOptional.isPresent()) {
            return CampoDeportivoMapper.toDto(campoOptional.get(), true);
        } else {
            throw new NotFoundException(CANCHAS, uuid);
        }
    }

    public List<CampoDeportivoDto> findCampoByComplejo(String uuidComplejo){
        List<CampoDeportivo> campos = campoRepository.findByComplejoUuid(uuidComplejo);

        if (!campos.isEmpty()) {
            return campos.stream()
                    .map(campo -> CampoDeportivoMapper.toDto(campo, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHAS);
        }
    }

    public List<CampoDeportivoDto> obtenerCampos(){
        List<CampoDeportivo> campos = campoRepository.findAll();

        if (!campos.isEmpty()) {
            return campos.stream()
                    .map(campo -> CampoDeportivoMapper.toDto(campo, true))
                    .collect(Collectors.toList());
        } else {
            throw new NoResultException(CANCHAS);
        }
    }

    @Transactional
    public CampoDeportivoDto crearCampo(CampoDeportivoDto campoDto) {
        if (campoDto == null) {
            throw new NullReferenceException(CANCHAS);
        }
        validarCampoDto(campoDto);
        CampoDeportivo nuevoCampo = CampoDeportivoMapper.toEntity(campoDto);
        ComplejoDeportivo complejo = obtenerComplejo(campoDto.getComplejoDto().getUuid());
        Superficie superficie = obtenerSuperficie(campoDto.getSuperficieDto().getUuid());
        nuevoCampo.setComplejo(complejo);
        nuevoCampo.setSuperficie(superficie);
        CampoDeportivo campoGuardado = campoRepository.save(nuevoCampo);
        if (campoDto.getTarifasDtoList() != null && !campoDto.getTarifasDtoList().isEmpty()) {
            crearTarifasParaCampo(campoGuardado, campoDto.getTarifasDtoList());
        }
        if (campoDto.getHorariosFijosDtoList() != null && !campoDto.getHorariosFijosDtoList().isEmpty()) {
            crearHorariosFijosParaCampo(campoGuardado, campoDto.getHorariosFijosDtoList());
        }
        if (campoDto.getHorariosExcepcionesDtoList() != null && !campoDto.getHorariosExcepcionesDtoList().isEmpty()) {
            crearHorariosExcepcionesParaCampo(campoGuardado, campoDto.getHorariosExcepcionesDtoList());
        }
        CampoDeportivo campoCompleto = campoRepository.findByUuid(campoGuardado.getUuid())
                .orElseThrow(() -> new NotFoundException(CANCHAS, campoGuardado.getUuid()));
        return CampoDeportivoMapper.toDto(campoCompleto, true);
    }

    @Transactional
    public CampoDeportivoDto actualizarCampo(CampoDeportivoDto campoDto){
        if (campoDto == null) {
            throw new NullReferenceException(CANCHAS);
        }
        validarCampoDto(campoDto);

        // Find existing entity by UUID
        Optional<CampoDeportivo> campoOptional = campoRepository.findByUuid(campoDto.getUuid());
        if (!campoOptional.isPresent()) {
            throw new NotFoundException(CANCHAS, campoDto.getUuid());
        }

        CampoDeportivo campoExistente = campoOptional.get();
        ComplejoDeportivo complejo = obtenerComplejo(campoDto.getComplejoDto().getUuid());
        Superficie superficie = obtenerSuperficie(campoDto.getSuperficieDto().getUuid());

        // Update the existing entity with new data - DON'T create a new one
        CampoDeportivoMapper.updateEntityFromDto(campoDto, campoExistente);
        campoExistente.setComplejo(complejo);
        campoExistente.setSuperficie(superficie);

        // Save the updated existing entity
        CampoDeportivo campoActualizado = campoRepository.save(campoExistente);

        // Update related entities
        if (campoDto.getTarifasDtoList() != null) {
            actualizarTarifas(campoActualizado, campoDto.getTarifasDtoList());
        }
        if (campoDto.getHorariosFijosDtoList() != null) {
            actualizarHorariosFijos(campoActualizado, campoDto.getHorariosFijosDtoList());
        }
        if (campoDto.getHorariosExcepcionesDtoList() != null) {
            actualizarHorariosExcepciones(campoActualizado, campoDto.getHorariosExcepcionesDtoList());
        }

        // Return the updated entity
        CampoDeportivo campoCompleto = campoRepository.findByUuid(campoActualizado.getUuid())
                .orElseThrow(() -> new NotFoundException(CANCHAS, campoActualizado.getUuid()));
        return CampoDeportivoMapper.toDto(campoCompleto, true);
    }

    @Transactional
    public String eliminarCampo(String uuid) {
        try {
            CampoDeportivo campo = campoRepository.findByUuid(uuid)
                    .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
            campo.setEstado(true);
            if (campo.getTarifasList() != null) {
                for (Tarifa tarifa : campo.getTarifasList()) {
                    tarifa.setEstado(true);
                    tarifaRepository.save(tarifa);
                    if (tarifa.getCategoriasList() != null) {
                        for (TarifaCategoria tarifaCat : tarifa.getCategoriasList()) {
                            tarifaCat.setEstado(true);
                            tarifaCategoriaRepository.save(tarifaCat);
                        }
                    }
                    if (tarifa.getConceptos() != null) {
                        for (TarifaConcepto concepto : tarifa.getConceptos()) {
                            concepto.setEstado(true);
                            tarifaConceptoRepository.save(concepto);
                        }
                    }
                }
            }
            if (campo.getHorariosFijosList() != null) {
                for (HorarioFijo horario : campo.getHorariosFijosList()) {
                    horario.setEstado(true);
                    horarioFijoRepository.save(horario);
                }
            }
            if (campo.getHorariosExcepcionesList() != null) {
                for (HorarioExcepcion horario : campo.getHorariosExcepcionesList()) {
                    horario.setEstado(true);
                    horarioExcepcionRepository.save(horario);
                }
            }
            campoRepository.save(campo);
            return "Desactivación exitosa de CANCHA y sus relaciones asociadas";
        } catch (DataAccessException e) {
            throw new RuntimeException("Error al intentar desactivar la CANCHA. Por favor, inténtelo de nuevo más tarde.", e);
        }
    }

    @Override
    @Transactional
    public String eliminarCampos(String uuid){
        CampoDeportivo campo = campoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(CANCHAS, uuid));
        if (campo.getReservasList() != null && !campo.getReservasList().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar un campo con reservas asociadas");
        }
        campoRepository.delete(campo);
        return "Eliminación exitosa";
    }

    public ComplejoDeportivo obtenerComplejo(String uuid){
        return complejoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(COMPLEJO, uuid));
    }

    public Superficie obtenerSuperficie(String uuid){
        return superficieRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException(SUPERFICIE, uuid));
    }

    private void validarCampoDto(CampoDeportivoDto campoDto) {
        if (campoDto.getNombre() == null || campoDto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del campo es obligatorio");
        }
        if (campoDto.getComplejoDto() == null || campoDto.getComplejoDto().getUuid() == null) {
            throw new IllegalArgumentException("El complejo deportivo es obligatorio");
        }
        if (campoDto.getSuperficieDto() == null || campoDto.getSuperficieDto().getUuid() == null) {
            throw new IllegalArgumentException("La superficie es obligatoria");
        }
    }

    private void crearTarifasParaCampo(CampoDeportivo campo, List<TarifaDto> tarifasDto) {
        for (TarifaDto tarifaDto : tarifasDto) {
            Tarifa nuevaTarifa = TarifaMapper.toEntity(tarifaDto);
            nuevaTarifa.setCampoDeportivo(campo);
            if (tarifaDto.getClasificadorRuatDto() == null || tarifaDto.getClasificadorRuatDto().getUuid() == null) {
                throw new IllegalArgumentException("El clasificador RUAT es obligatorio para la tarifa");
            }
            ClasificadorRuat clasificador = clasificadorRuatRepository.findByUuid(tarifaDto.getClasificadorRuatDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, tarifaDto.getClasificadorRuatDto().getUuid()));
            nuevaTarifa.setClasificadorRuat(clasificador);
            Tarifa tarifaGuardada = tarifaRepository.save(nuevaTarifa);
            if (tarifaDto.getCategoriasDtoList() != null && !tarifaDto.getCategoriasDtoList().isEmpty()) {
                crearTarifaCategorias(tarifaGuardada, tarifaDto.getCategoriasDtoList());
            }
            if (tarifaDto.getConceptosDtos() != null && !tarifaDto.getConceptosDtos().isEmpty()) {
                crearTarifaConceptos(tarifaGuardada, tarifaDto.getConceptosDtos());
            }
        }
    }
    private void crearTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> tarifaCategoriasDto) {
        for (TarifaCategoriaDto dto : tarifaCategoriasDto) {
            if (dto.getCategoriaDto() == null || dto.getCategoriaDto().getUuid() == null) {
                throw new IllegalArgumentException("La categoría es obligatoria");
            }
            TarifaCategoria nueva = TarifaCategoriaMapper.toEntity(dto);
            nueva.setTarifa(tarifa);
            CategoriaDeportiva categoria = categoriaRepository.findByUuid(
                    dto.getCategoriaDto().getUuid()
            ).orElseThrow(() -> new NotFoundException(CATEGORIA, dto.getCategoriaDto().getUuid()));
            nueva.setCategoria(categoria);
            tarifaCategoriaRepository.save(nueva);
        }
    }

    private void crearTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> tarifaConceptosDto) {
        for (TarifaConceptoDto tarifaConceptoDto : tarifaConceptosDto) {
            TarifaConcepto nuevoConcepto = TarifaConceptoMapper.toEntity(tarifaConceptoDto);
            nuevoConcepto.setTarifa(tarifa);
            tarifaConceptoRepository.save(nuevoConcepto);
        }
    }

    private void crearHorariosFijosParaCampo(CampoDeportivo campo, List<HorarioFijoDto> horariosFijosDto) {
        for (HorarioFijoDto horarioFijoDto : horariosFijosDto) {
            HorarioFijo nuevoHorario = HorarioFijoMapper.toEntity(horarioFijoDto);
            nuevoHorario.setCampoDeportivo(campo);
            horarioFijoRepository.save(nuevoHorario);
        }
    }

    private void crearHorariosExcepcionesParaCampo(CampoDeportivo campo, List<HorarioExcepcionDto> horariosExcepcionesDto) {
        for (HorarioExcepcionDto horarioExcepcionDto : horariosExcepcionesDto) {
            HorarioExcepcion nuevaExcepcion = HorarioExcepcionMapper.toEntity(horarioExcepcionDto);
            nuevaExcepcion.setCampoDeportivo(campo);
            horarioExcepcionRepository.save(nuevaExcepcion);
        }
    }

// Add these methods to your CampoServiceImpl class

    private void actualizarTarifas(CampoDeportivo campo, List<TarifaDto> tarifasDto) {
        // Build map of incoming tarifa UUIDs
        Map<String, TarifaDto> tarifasDtoMap = new HashMap<>();
        for (TarifaDto dto : tarifasDto) {
            if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
                tarifasDtoMap.put(dto.getUuid(), dto);
            }
        }

        // Process existing tarifas
        List<Tarifa> tarifasToDelete = new ArrayList<>();
        if (campo.getTarifasList() != null) {
            for (Tarifa tarifaExistente : campo.getTarifasList()) {
                if (tarifasDtoMap.containsKey(tarifaExistente.getUuid())) {
                    // UPDATE existing tarifa
                    TarifaDto tarifaDto = tarifasDtoMap.get(tarifaExistente.getUuid());
                    actualizarTarifaExistente(tarifaExistente, tarifaDto);
                    tarifasDtoMap.remove(tarifaExistente.getUuid()); // Remove from map
                } else {
                    // Mark for soft delete (not in the incoming list)
                    tarifasToDelete.add(tarifaExistente);
                }
            }
        }

        // Soft delete tarifas not in the incoming list
        for (Tarifa tarifa : tarifasToDelete) {
            softDeleteTarifa(tarifa);
        }

        // CREATE new tarifas (remaining in map are new)
        List<TarifaDto> nuevasTarifas = new ArrayList<>();
        for (TarifaDto dto : tarifasDto) {
            if (dto.getUuid() == null || dto.getUuid().isEmpty() || tarifasDtoMap.containsKey(dto.getUuid())) {
                nuevasTarifas.add(dto);
            }
        }
        if (!nuevasTarifas.isEmpty()) {
            crearTarifasParaCampo(campo, nuevasTarifas);
        }
    }

    private void actualizarTarifaExistente(Tarifa tarifaExistente, TarifaDto tarifaDto) {
        // Update basic fields
        tarifaExistente.setPrecioBase(tarifaDto.getPrecioBase());
        tarifaExistente.setFechaInicio(tarifaDto.getFechaInicio());
        tarifaExistente.setFechaFin(tarifaDto.getFechaFin());
        tarifaExistente.setDesde(tarifaDto.getDesde());
        tarifaExistente.setHasta(tarifaDto.getHasta());
        tarifaExistente.setActivo(tarifaDto.getActivo());
        tarifaExistente.setDescripcion(tarifaDto.getDescripcion());
        tarifaExistente.setObservaciones(tarifaDto.getObservaciones());

        // Update clasificador if changed
        if (tarifaDto.getClasificadorRuatDto() != null && tarifaDto.getClasificadorRuatDto().getUuid() != null) {
            ClasificadorRuat clasificador = clasificadorRuatRepository.findByUuid(tarifaDto.getClasificadorRuatDto().getUuid())
                    .orElseThrow(() -> new NotFoundException(CLASIFICADOR_RUAT, tarifaDto.getClasificadorRuatDto().getUuid()));
            tarifaExistente.setClasificadorRuat(clasificador);
        }

        tarifaRepository.save(tarifaExistente);

        // Update categorias
        actualizarTarifaCategorias(tarifaExistente, tarifaDto.getCategoriasDtoList());

        // Update conceptos
        if (tarifaDto.getConceptosDtos() != null) {
            actualizarTarifaConceptos(tarifaExistente, tarifaDto.getConceptosDtos());
        }
    }

    private void softDeleteTarifa(Tarifa tarifa) {
        tarifa.setEstado(true);
        tarifaRepository.save(tarifa);

        // Soft delete related entities
        if (tarifa.getCategoriasList() != null) {
            for (TarifaCategoria cat : tarifa.getCategoriasList()) {
                cat.setEstado(true);
                tarifaCategoriaRepository.save(cat);
            }
        }
        if (tarifa.getConceptos() != null) {
            for (TarifaConcepto concepto : tarifa.getConceptos()) {
                concepto.setEstado(true);
                tarifaConceptoRepository.save(concepto);
            }
        }
    }

    private void actualizarTarifaCategorias(Tarifa tarifa, List<TarifaCategoriaDto> categoriasDtoList) {
        if (categoriasDtoList == null) {
            return;
        }

        // Build set of incoming categoria UUIDs
        Set<String> categoriaUuidsIncoming = new HashSet<>();
        for (TarifaCategoriaDto dto : categoriasDtoList) {
            if (dto.getCategoriaDto() != null && dto.getCategoriaDto().getUuid() != null) {
                categoriaUuidsIncoming.add(dto.getCategoriaDto().getUuid());
            }
        }

        // Soft delete categorias not in the incoming list
        if (tarifa.getCategoriasList() != null) {
            for (TarifaCategoria existente : tarifa.getCategoriasList()) {
                String existingCatUuid = existente.getCategoria() != null ? existente.getCategoria().getUuid() : null;
                if (existingCatUuid != null && !categoriaUuidsIncoming.contains(existingCatUuid)) {
                    existente.setEstado(true);
                    tarifaCategoriaRepository.save(existente);
                }
            }
        }

        // Create new TarifaCategorias for new categorias
        Set<String> existingCatUuids = new HashSet<>();
        if (tarifa.getCategoriasList() != null) {
            for (TarifaCategoria existente : tarifa.getCategoriasList()) {
                if (existente.getCategoria() != null && !existente.isEstado()) {
                    existingCatUuids.add(existente.getCategoria().getUuid());
                }
            }
        }

        for (TarifaCategoriaDto dto : categoriasDtoList) {
            if (dto.getCategoriaDto() != null && dto.getCategoriaDto().getUuid() != null) {
                String catUuid = dto.getCategoriaDto().getUuid();
                if (!existingCatUuids.contains(catUuid)) {
                    // Create new TarifaCategoria
                    TarifaCategoria nueva = new TarifaCategoria();
                    nueva.setTarifa(tarifa);
                    CategoriaDeportiva categoria = categoriaRepository.findByUuid(catUuid)
                            .orElseThrow(() -> new NotFoundException(CATEGORIA, catUuid));
                    nueva.setCategoria(categoria);
                    tarifaCategoriaRepository.save(nueva);
                }
            }
        }
    }

    private void actualizarTarifaConceptos(Tarifa tarifa, List<TarifaConceptoDto> conceptosDtoList) {
        // Soft delete all existing conceptos
        if (tarifa.getConceptos() != null) {
            for (TarifaConcepto concepto : tarifa.getConceptos()) {
                concepto.setEstado(true);
                tarifaConceptoRepository.save(concepto);
            }
        }

        // Create new conceptos
        if (conceptosDtoList != null && !conceptosDtoList.isEmpty()) {
            crearTarifaConceptos(tarifa, conceptosDtoList);
        }
    }

    private void actualizarHorariosFijos(CampoDeportivo campo, List<HorarioFijoDto> horariosDto) {
        // Build map of incoming horarios by UUID
        Map<String, HorarioFijoDto> horariosDtoMap = new HashMap<>();
        for (HorarioFijoDto dto : horariosDto) {
            if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
                horariosDtoMap.put(dto.getUuid(), dto);
            }
        }

        // Process existing horarios
        List<HorarioFijo> horariosToDelete = new ArrayList<>();
        if (campo.getHorariosFijosList() != null) {
            for (HorarioFijo horarioExistente : campo.getHorariosFijosList()) {
                if (horariosDtoMap.containsKey(horarioExistente.getUuid())) {
                    // UPDATE existing horario
                    HorarioFijoDto dto = horariosDtoMap.get(horarioExistente.getUuid());
                    actualizarHorarioFijoExistente(horarioExistente, dto);
                    horariosDtoMap.remove(horarioExistente.getUuid());
                } else {
                    // Mark for deletion
                    horariosToDelete.add(horarioExistente);
                }
            }
        }

        // Soft delete horarios not in the incoming list
        for (HorarioFijo horario : horariosToDelete) {
            horario.setEstado(true);
            horarioFijoRepository.save(horario);
        }

        // CREATE new horarios (remaining in map or without UUID)
        List<HorarioFijoDto> nuevosHorarios = new ArrayList<>();
        for (HorarioFijoDto dto : horariosDto) {
            if (dto.getUuid() == null || dto.getUuid().isEmpty() || horariosDtoMap.containsKey(dto.getUuid())) {
                nuevosHorarios.add(dto);
            }
        }
        if (!nuevosHorarios.isEmpty()) {
            crearHorariosFijosParaCampo(campo, nuevosHorarios);
        }
    }

    private void actualizarHorarioFijoExistente(HorarioFijo horarioExistente, HorarioFijoDto dto) {
        horarioExistente.setDia(dto.getDia());
        horarioExistente.setAbierto(dto.isAbierto());
        horarioExistente.setHoraApertura(dto.getHoraApertura());
        horarioExistente.setHoraCierre(dto.getHoraCierre());
        horarioExistente.setObservaciones(dto.getObservaciones());
        horarioFijoRepository.save(horarioExistente);
    }

    private void actualizarHorariosExcepciones(CampoDeportivo campo, List<HorarioExcepcionDto> horariosDto) {
        // Build map of incoming excepciones by UUID
        Map<String, HorarioExcepcionDto> horariosDtoMap = new HashMap<>();
        for (HorarioExcepcionDto dto : horariosDto) {
            if (dto.getUuid() != null && !dto.getUuid().isEmpty()) {
                horariosDtoMap.put(dto.getUuid(), dto);
            }
        }

        // Process existing excepciones
        List<HorarioExcepcion> horariosToDelete = new ArrayList<>();
        if (campo.getHorariosExcepcionesList() != null) {
            for (HorarioExcepcion horarioExistente : campo.getHorariosExcepcionesList()) {
                if (horariosDtoMap.containsKey(horarioExistente.getUuid())) {
                    // UPDATE existing excepcion
                    HorarioExcepcionDto dto = horariosDtoMap.get(horarioExistente.getUuid());
                    actualizarHorarioExcepcionExistente(horarioExistente, dto);
                    horariosDtoMap.remove(horarioExistente.getUuid());
                } else {
                    // Mark for deletion
                    horariosToDelete.add(horarioExistente);
                }
            }
        }

        // Soft delete excepciones not in the incoming list
        for (HorarioExcepcion horario : horariosToDelete) {
            horario.setEstado(true);
            horarioExcepcionRepository.save(horario);
        }

        // CREATE new excepciones
        List<HorarioExcepcionDto> nuevosHorarios = new ArrayList<>();
        for (HorarioExcepcionDto dto : horariosDto) {
            if (dto.getUuid() == null || dto.getUuid().isEmpty() || horariosDtoMap.containsKey(dto.getUuid())) {
                nuevosHorarios.add(dto);
            }
        }
        if (!nuevosHorarios.isEmpty()) {
            crearHorariosExcepcionesParaCampo(campo, nuevosHorarios);
        }
    }

    private void actualizarHorarioExcepcionExistente(HorarioExcepcion horarioExistente, HorarioExcepcionDto dto) {
        horarioExistente.setFecha(dto.getFecha());
        horarioExistente.setTipoExcepcion(dto.getTipoExcepcion());
        horarioExistente.setHoraApertura(dto.getHoraApertura());
        horarioExistente.setHoraCierre(dto.getHoraCierre());
        horarioExistente.setMotivo(dto.getMotivo());
        horarioExcepcionRepository.save(horarioExistente);
    }
}