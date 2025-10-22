package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import com.gamq.deportes.dto.mapper.ConfiguracionSistemaMapper;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.service.ConfiguracionSistemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionSistemaServiceImpl implements ConfiguracionSistemaService {

    private final ConfiguracionSistemaRepository configuracionSistemaRepository;

    @Override
    public ConfiguracionSistemaDto findByUuid(String uuid) {
        log.debug("Buscando configuración por UUID: {}", uuid);
        return configuracionSistemaRepository.findByUuid(uuid)
                .map(ConfiguracionSistemaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));
    }

    @Override
    public ConfiguracionSistemaDto findByClave(String clave) {
        log.debug("Buscando configuración por clave: {}", clave);
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .map(ConfiguracionSistemaMapper::toDto)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con clave: " + clave));
    }

    @Override
    public List<ConfiguracionSistemaDto> findByCategoria(String categoria) {
        log.debug("Buscando configuraciones por categoría: {}", categoria);
        return configuracionSistemaRepository.findByCategoriaAndEstadoFalse(categoria)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> findByAplicaA(String aplicaA) {
        log.debug("Buscando configuraciones por aplicaA: {}", aplicaA);
        return configuracionSistemaRepository.findByAplicaAAndEstadoFalse(aplicaA)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> findByReferenciaUuid(String referenciaUuid) {
        log.debug("Buscando configuraciones por referencia UUID: {}", referenciaUuid);
        return configuracionSistemaRepository.findByReferenciaUuidAndEstadoFalse(referenciaUuid)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguraciones() {
        log.debug("Obteniendo todas las configuraciones activas");
        return configuracionSistemaRepository.findAllActivas()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguracionesActivas() {
        log.debug("Obteniendo configuraciones activas");
        return configuracionSistemaRepository.findAllActivas()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguracionesModificablesPorUsuario() {
        log.debug("Obteniendo configuraciones modificables por usuario");
        return configuracionSistemaRepository.findModificablesPorUsuario()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto crearConfiguracion(ConfiguracionSistemaDto configuracionDto) {
        log.debug("Creando nueva configuración: {}", configuracionDto.getClave());

        // Validar que no exista una configuración con la misma clave
        if (configuracionSistemaRepository.findByClave(configuracionDto.getClave()).isPresent()) {
            throw new RuntimeException("Ya existe una configuración con la clave: " + configuracionDto.getClave());
        }

        ConfiguracionSistema configuracion = ConfiguracionSistemaMapper.toEntity(configuracionDto);
        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracion);

        log.info("Configuración creada exitosamente: {}", configuracionDto.getClave());
        return ConfiguracionSistemaMapper.toDto(configuracionGuardada);
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto actualizarConfiguracion(ConfiguracionSistemaDto configuracionDto) {
        log.debug("Actualizando configuración con UUID: {}", configuracionDto.getUuid());

        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByUuid(configuracionDto.getUuid())
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + configuracionDto.getUuid()));

        // Validar si requiere aprobación
        if (Boolean.TRUE.equals(configuracionExistente.getRequiereAprobacion())) {
            log.warn("Configuración {} requiere aprobación para ser modificada", configuracionDto.getClave());
            // Aquí podrías lanzar una excepción o manejar el flujo de aprobación
        }

        ConfiguracionSistema configuracionActualizada = ConfiguracionSistemaMapper.updateEntityFromDto(configuracionDto, configuracionExistente);
        configuracionActualizada.setUltimaModificacion(LocalDateTime.now());
        // Aquí deberías setear el usuario que modificó: configuracionActualizada.setModificadoPor(usuarioActual);

        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracionActualizada);

        log.info("Configuración actualizada exitosamente: {}", configuracionDto.getClave());
        return ConfiguracionSistemaMapper.toDto(configuracionGuardada);
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto actualizarConfiguracionPorClave(String clave, ConfiguracionSistemaDto configuracionDto) {
        log.debug("Actualizando configuración por clave: {}", clave);

        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con clave: " + clave));

        // Validar que el usuario pueda modificar esta configuración
        if (!Boolean.TRUE.equals(configuracionExistente.getModificablePorUsuario())) {
            throw new RuntimeException("La configuración " + clave + " no es modificable por usuarios");
        }

        // Actualizar solo los campos permitidos
        if (configuracionDto.getValorNumerico() != null) {
            configuracionExistente.setValorNumerico(configuracionDto.getValorNumerico());
        }
        if (configuracionDto.getValorTexto() != null) {
            configuracionExistente.setValorTexto(configuracionDto.getValorTexto());
        }
        if (configuracionDto.getValorBoolean() != null) {
            configuracionExistente.setValorBoolean(configuracionDto.getValorBoolean());
        }
        if (configuracionDto.getValorJson() != null) {
            configuracionExistente.setValorJson(configuracionDto.getValorJson());
        }

        configuracionExistente.setUltimaModificacion(LocalDateTime.now());
        // configuracionExistente.setModificadoPor(usuarioActual);

        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracionExistente);

        log.info("Configuración actualizada por clave exitosamente: {}", clave);
        return ConfiguracionSistemaMapper.toDto(configuracionGuardada);
    }

    @Override
    @Transactional
    public String eliminarConfiguracion(String uuid) {
        log.debug("Eliminando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        configuracionSistemaRepository.delete(configuracion);

        log.info("Configuración eliminada exitosamente: {}", uuid);
        return "Configuración eliminada exitosamente";
    }

    @Override
    @Transactional
    public String desactivarConfiguracion(String uuid) {
        log.debug("Desactivando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        configuracion.setActivo(false);
        configuracion.setUltimaModificacion(LocalDateTime.now());
        configuracionSistemaRepository.save(configuracion);

        log.info("Configuración desactivada exitosamente: {}", uuid);
        return "Configuración desactivada exitosamente";
    }

    // Métodos utilitarios para obtener valores específicos
    @Override
    public String obtenerValorTexto(String clave, String valorPorDefecto) {
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .map(ConfiguracionSistema::getValorTexto)
                .orElse(valorPorDefecto);
    }

    @Override
    public Double obtenerValorNumerico(String clave, Double valorPorDefecto) {
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .map(ConfiguracionSistema::getValorNumerico)
                .orElse(valorPorDefecto);
    }

    @Override
    public Boolean obtenerValorBoolean(String clave, Boolean valorPorDefecto) {
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .map(ConfiguracionSistema::getValorBoolean)
                .orElse(valorPorDefecto);
    }

    @Override
    public Map<String, Object> obtenerConfiguracionesPorClaves(List<String> claves) {
        return configuracionSistemaRepository.findByClaves(claves)
                .stream()
                .collect(Collectors.toMap(
                        ConfiguracionSistema::getClave,
                        config -> {
                            if (config.getValorTexto() != null) return config.getValorTexto();
                            if (config.getValorNumerico() != null) return config.getValorNumerico();
                            if (config.getValorBoolean() != null) return config.getValorBoolean();
                            if (config.getValorJson() != null) return config.getValorJson();
                            return null;
                        }
                ));
    }

    @Override
    public List<String> obtenerCategorias() {
        return configuracionSistemaRepository.findDistinctCategorias();
    }

    @Override
    public Map<String, List<ConfiguracionSistemaDto>> obtenerConfiguracionesAgrupadasPorCategoria() {
        return configuracionSistemaRepository.findAllActivas()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .collect(Collectors.groupingBy(ConfiguracionSistemaDto::getCategoria));
    }
}