package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import com.gamq.deportes.dto.mapper.ConfiguracionSistemaMapper;
import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.service.ConfiguracionSistemaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gamq.deportes.security.DeportesPrincipal;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionSistemaServiceImpl implements ConfiguracionSistemaService {

    private static final String CATEGORIA_RUAT = "RUAT";
    private static final String GLOBAL = "global";

    private final ConfiguracionSistemaRepository configuracionSistemaRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ConfiguracionSistemaDto findByUuid(String uuid) {
        log.debug("Buscando configuración por UUID: {}", uuid);
        return configuracionSistemaRepository.findByUuid(uuid)
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));
    }

    @Override
    public ConfiguracionSistemaDto findByClave(String clave) {
        log.debug("Buscando configuración por clave: {}", clave);
        return configuracionSistemaRepository.findByClaveAndEstadoFalse(clave)
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con clave: " + clave));
    }

    @Override
    public List<ConfiguracionSistemaDto> findByCategoria(String categoria) {
        log.debug("Buscando configuraciones por categoría: {}", categoria);
        return configuracionSistemaRepository.findByCategoriaAndEstadoFalse(categoria)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> findByAplicaA(String aplicaA) {
        log.debug("Buscando configuraciones por aplicaA: {}", aplicaA);
        return configuracionSistemaRepository.findByAplicaAAndEstadoFalse(aplicaA)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> findByReferenciaUuid(String referenciaUuid) {
        log.debug("Buscando configuraciones por referencia UUID: {}", referenciaUuid);
        return configuracionSistemaRepository.findByReferenciaUuidAndEstadoFalse(referenciaUuid)
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguraciones() {
        log.debug("Obteniendo todas las configuraciones activas");
        return configuracionSistemaRepository.findAllActivas()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguracionesActivas() {
        log.debug("Obteniendo configuraciones activas");
        return configuracionSistemaRepository.findAllActivas()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguracionesModificablesPorUsuario() {
        log.debug("Obteniendo configuraciones modificables por usuario");
        return configuracionSistemaRepository.findModificablesPorUsuario()
                .stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto crearConfiguracion(ConfiguracionSistemaDto configuracionDto) {
        log.debug("Creando nueva configuración: {}", configuracionDto.getClave());

        normalizeAndValidate(configuracionDto);
        if (configuracionSistemaRepository.countByClaveAndScope(
                configuracionDto.getClave(),
                configuracionDto.getAplicaA(),
                configuracionDto.getReferenciaUuid()) > 0) {
            throw new RuntimeException("Ya existe una configuración con la misma clave y alcance");
        }

        ConfiguracionSistema configuracion = ConfiguracionSistemaMapper.toEntity(configuracionDto);
        LocalDateTime now = LocalDateTime.now();
        configuracion.setEstado(false);
        configuracion.setFechaCreacion(now);
        configuracion.setFechaActualizacion(now);
        configuracion.setUltimaModificacion(now);
        configuracion.setModificadoPor(currentActor());
        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracion);

        log.info("Configuración creada exitosamente: {}", configuracionDto.getClave());
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto actualizarConfiguracion(ConfiguracionSistemaDto configuracionDto) {
        log.debug("Actualizando configuración con UUID: {}", configuracionDto.getUuid());

        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByUuid(configuracionDto.getUuid())
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + configuracionDto.getUuid()));

        normalizeAndValidate(configuracionDto);
        if (Boolean.TRUE.equals(configuracionExistente.getRequiereAprobacion())
                && hasValueChange(configuracionExistente, configuracionDto)) {
            throw new IllegalStateException(
                    "La configuración " + configuracionExistente.getClave()
                            + " requiere aprobación y no admite actualización directa");
        }

        ConfiguracionSistema configuracionActualizada = ConfiguracionSistemaMapper.updateEntityFromDto(configuracionDto, configuracionExistente);
        configuracionActualizada.setUltimaModificacion(LocalDateTime.now());
        configuracionActualizada.setModificadoPor(currentActor());

        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracionActualizada);

        log.info("Configuración actualizada exitosamente: {}", configuracionDto.getClave());
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
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
        validateValueUpdate(configuracionExistente, configuracionDto);

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
        configuracionExistente.setModificadoPor(currentActor());

        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracionExistente);

        log.info("Configuración actualizada por clave exitosamente: {}", clave);
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
    }

    @Override
    @Transactional
    public String eliminarConfiguracion(String uuid) {
        log.debug("Eliminando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        configuracion.setEstado(true);
        configuracion.setActivo(false);
        configuracion.setUltimaModificacion(LocalDateTime.now());
        configuracion.setModificadoPor(currentActor());
        configuracionSistemaRepository.save(configuracion);

        log.info("Configuración archivada mediante borrado lógico: {}", uuid);
        return "Configuración archivada exitosamente";
    }

    @Override
    @Transactional
    public String desactivarConfiguracion(String uuid) {
        log.debug("Desactivando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        configuracion.setActivo(false);
        configuracion.setUltimaModificacion(LocalDateTime.now());
        configuracion.setModificadoPor(currentActor());
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
                .map(this::sanitizeForClient)
                .collect(Collectors.groupingBy(ConfiguracionSistemaDto::getCategoria));
    }

    private ConfiguracionSistemaDto sanitizeForClient(ConfiguracionSistemaDto dto) {
        if (dto == null || !CATEGORIA_RUAT.equalsIgnoreCase(dto.getCategoria())) {
            return dto;
        }
        dto.setValorTexto(null);
        dto.setValorNumerico(null);
        dto.setValorBoolean(null);
        dto.setValorJson(null);
        dto.setValoresPosibles(null);
        return dto;
    }

    private void normalizeAndValidate(ConfiguracionSistemaDto dto) {
        if (dto == null || dto.getClave() == null || dto.getClave().trim().isEmpty()) {
            throw new IllegalArgumentException("La clave de configuración es requerida");
        }
        dto.setClave(dto.getClave().trim());
        dto.setAplicaA(normalizeScope(dto.getAplicaA()));
        dto.setReferenciaUuid(normalizeReference(dto.getReferenciaUuid()));
        if (GLOBAL.equals(dto.getAplicaA()) && dto.getReferenciaUuid() != null) {
            throw new IllegalArgumentException("Una configuración global no puede tener referenciaUuid");
        }
        validateValueShape(dto);
    }

    private void validateValueUpdate(ConfiguracionSistema existing, ConfiguracionSistemaDto dto) {
        ConfiguracionSistemaDto candidate = ConfiguracionSistemaMapper.toDto(existing);
        if (dto.getValorTexto() != null) candidate.setValorTexto(dto.getValorTexto());
        if (dto.getValorNumerico() != null) candidate.setValorNumerico(dto.getValorNumerico());
        if (dto.getValorBoolean() != null) candidate.setValorBoolean(dto.getValorBoolean());
        if (dto.getValorJson() != null) candidate.setValorJson(dto.getValorJson());
        validateValueShape(candidate);
    }

    private void validateValueShape(ConfiguracionSistemaDto dto) {
        int populated = 0;
        if (dto.getValorTexto() != null) populated++;
        if (dto.getValorNumerico() != null) populated++;
        if (dto.getValorBoolean() != null) populated++;
        if (dto.getValorJson() != null) populated++;
        if (populated != 1) {
            throw new IllegalArgumentException("La configuración debe tener exactamente un tipo de valor");
        }
        if (dto.getValorMinimo() != null && dto.getValorMaximo() != null
                && dto.getValorMinimo() > dto.getValorMaximo()) {
            throw new IllegalArgumentException("El valor mínimo no puede ser mayor al valor máximo");
        }
        if (dto.getValorNumerico() != null) {
            if (dto.getValorMinimo() != null && dto.getValorNumerico() < dto.getValorMinimo()) {
                throw new IllegalArgumentException("El valor numérico es menor al mínimo permitido");
            }
            if (dto.getValorMaximo() != null && dto.getValorNumerico() > dto.getValorMaximo()) {
                throw new IllegalArgumentException("El valor numérico es mayor al máximo permitido");
            }
        }
        validateJson(dto.getValorJson(), "valorJson");
        validateJson(dto.getValoresPosibles(), "valoresPosibles");
    }

    private void validateJson(String value, String field) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            objectMapper.readTree(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(field + " debe contener JSON válido");
        }
    }

    private boolean hasValueChange(ConfiguracionSistema existing, ConfiguracionSistemaDto dto) {
        return !Objects.equals(existing.getValorTexto(), dto.getValorTexto())
                || !Objects.equals(existing.getValorNumerico(), dto.getValorNumerico())
                || !Objects.equals(existing.getValorBoolean(), dto.getValorBoolean())
                || !Objects.equals(existing.getValorJson(), dto.getValorJson());
    }

    private String normalizeScope(String aplicaA) {
        return aplicaA == null || aplicaA.trim().isEmpty()
                ? GLOBAL
                : aplicaA.trim().toLowerCase();
    }

    private String normalizeReference(String referenciaUuid) {
        return referenciaUuid == null || referenciaUuid.trim().isEmpty()
                ? null
                : referenciaUuid.trim();
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal) {
            String subject = ((DeportesPrincipal) authentication.getPrincipal()).getSubject();
            if (subject != null && !subject.trim().isEmpty()) {
                return subject.trim();
            }
        }
        return "SYSTEM";
    }
}
