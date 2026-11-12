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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfiguracionSistemaServiceImpl implements ConfiguracionSistemaService {

    private static final String CATEGORIA_RUAT = "RUAT";
    private static final String GLOBAL = "global";
    private static final Set<String> CATEGORIAS_CLIENTE = Set.of(
            "DATOS_REFERENCIA", "REGLAS_RESERVA", "POLITICAS_PAGO", "LIMITES", "TIMEOUTS");
    private static final Set<String> AMBITOS_PERMITIDOS = Set.of(
            GLOBAL, "contribuyente", "representante");

    private final ConfiguracionSistemaRepository configuracionSistemaRepository;
    private final ObjectMapper objectMapper;
    private final com.gamq.deportes.service.SistemaAuditoriaService auditoriaService;
    private final com.gamq.deportes.security.ConfiguracionResolver configuracionResolver;

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
        List<ConfiguracionSistema> configuraciones = configuracionSistemaRepository.findAllActivas();
        if (!configuracionResolver.esAdmin()) {
            // Only deliver settings that apply to the caller's audience. Admins
            // keep the full catalogue because they manage every scope.
            List<String> audiencias = configuracionResolver.audienciasAplicables();
            configuraciones = configuraciones.stream()
                    .filter(config -> config.getReferenciaUuid() == null
                            && audiencias.contains(config.getAplicaA()))
                    .collect(Collectors.toList());
        }
        return configuraciones.stream()
                .map(ConfiguracionSistemaMapper::toDto)
                .map(this::sanitizeForClient)
                .collect(Collectors.toList());
    }

    /**
     * Snapshot embedded in the authentication response. Permission flags are
     * resolved into the menu before this point; RUAT and notification values
     * are server-only. The UI receives just global, non-sensitive values it
     * uses to present reservation rules and reference data.
     */
    @Override
    public List<ConfiguracionSistemaDto> obtenerConfiguracionesParaCliente() {
        return configuracionSistemaRepository.findAllActivas().stream()
                .filter(config -> CATEGORIAS_CLIENTE.contains(config.getCategoria()))
                .filter(config -> GLOBAL.equalsIgnoreCase(config.getAplicaA()))
                .filter(config -> config.getReferenciaUuid() == null)
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
        auditar("CREATE", null, configuracionGuardada);

        log.info("Configuración creada exitosamente: {}", configuracionDto.getClave());
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto actualizarConfiguracion(ConfiguracionSistemaDto configuracionDto) {
        log.debug("Actualizando configuración con UUID: {}", configuracionDto.getUuid());

        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByUuid(configuracionDto.getUuid())
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + configuracionDto.getUuid()));

        requireEditable(configuracionExistente);
        String anterior = snapshotAuditoria(configuracionExistente);
        normalizeAndValidate(configuracionDto);
        requireNoScopeCollision(configuracionDto, configuracionExistente.getUuid());
        ConfiguracionSistema configuracionActualizada = ConfiguracionSistemaMapper.updateEntityFromDto(configuracionDto, configuracionExistente);
        configuracionActualizada.setUltimaModificacion(LocalDateTime.now());
        configuracionActualizada.setModificadoPor(currentActor());

        ConfiguracionSistema configuracionGuardada = configuracionSistemaRepository.save(configuracionActualizada);
        auditar("UPDATE", anterior, configuracionGuardada);

        log.info("Configuración actualizada exitosamente: {}", configuracionDto.getClave());
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
    }

    @Override
    @Transactional
    public ConfiguracionSistemaDto actualizarConfiguracionPorClave(String clave, ConfiguracionSistemaDto configuracionDto) {
        log.debug("Actualizando configuración por clave: {}", clave);

        ConfiguracionSistema configuracionExistente = configuracionSistemaRepository.findByClaveAndEstadoFalseAnyScope(clave)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con clave: " + clave));

        // Validar que el usuario pueda modificar esta configuración
        requireEditable(configuracionExistente);
        validateValueUpdate(configuracionExistente, configuracionDto);
        String anterior = snapshotAuditoria(configuracionExistente);

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
        auditar("UPDATE", anterior, configuracionGuardada);

        log.info("Configuración actualizada por clave exitosamente: {}", clave);
        return sanitizeForClient(ConfiguracionSistemaMapper.toDto(configuracionGuardada));
    }

    @Override
    @Transactional
    public String eliminarConfiguracion(String uuid) {
        log.debug("Eliminando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        String anterior = snapshotAuditoria(configuracion);
        configuracion.setEstado(true);
        configuracion.setActivo(false);
        configuracion.setUltimaModificacion(LocalDateTime.now());
        configuracion.setModificadoPor(currentActor());
        configuracionSistemaRepository.save(configuracion);
        auditar("DELETE", anterior, configuracion);

        log.info("Configuración archivada mediante borrado lógico: {}", uuid);
        return "Configuración archivada exitosamente";
    }

    @Override
    @Transactional
    public String desactivarConfiguracion(String uuid) {
        log.debug("Desactivando configuración con UUID: {}", uuid);

        ConfiguracionSistema configuracion = configuracionSistemaRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada con UUID: " + uuid));

        String anterior = snapshotAuditoria(configuracion);
        configuracion.setActivo(false);
        configuracion.setUltimaModificacion(LocalDateTime.now());
        configuracion.setModificadoPor(currentActor());
        configuracionSistemaRepository.save(configuracion);
        auditar("DEACTIVATE", anterior, configuracion);

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

    private String snapshotAuditoria(ConfiguracionSistema config) {
        ConfiguracionSistemaDto dto = sanitizeForClient(ConfiguracionSistemaMapper.toDto(config));
        // Never copy credentials into the audit history, even outside the RUAT category.
        String key = config.getClave() == null ? "" : config.getClave().toUpperCase(java.util.Locale.ROOT);
        if (key.matches(".*(PASSWORD|SECRET|TOKEN|CREDENTIAL|API_KEY|CONTRASENA|CONTRASEÑA).*")) {
            dto.setValorTexto(null);
            dto.setValorNumerico(null);
            dto.setValorBoolean(null);
            dto.setValorJson(null);
            dto.setValoresPosibles(null);
        }
        return objectMapper.valueToTree(dto).toString();
    }

    private void auditar(String accion, String anterior, ConfiguracionSistema config) {
        String cambios = "{\"anterior\":" + (anterior == null ? "null" : anterior)
                + ",\"nuevo\":" + snapshotAuditoria(config) + "}";
        auditoriaService.registrarAccion(null, accion, "CONFIGURACIONES", "CONFIGURACION_SISTEMA",
                config.getUuid(), "Configuración: " + config.getClave(), cambios, null, null);
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
        if (!AMBITOS_PERMITIDOS.contains(dto.getAplicaA())) {
            throw new IllegalArgumentException("Ámbito de configuración no permitido: " + dto.getAplicaA());
        }
        if (dto.getReferenciaUuid() != null) {
            throw new IllegalArgumentException(
                    "Las referencias por entidad no están habilitadas. Use un ámbito de audiencia (global, contribuyente o representante)");
        }
        validateValueShape(dto);
    }

    private void requireEditable(ConfiguracionSistema existing) {
        if (configuracionResolver.esAdmin()) {
            return;
        }
        if (!Boolean.TRUE.equals(existing.getModificablePorUsuario())) {
            throw new IllegalStateException(
                    "La configuración " + existing.getClave() + " no es modificable por usuarios");
        }
    }

    private void requireNoScopeCollision(ConfiguracionSistemaDto dto, String currentUuid) {
        Long collisions = currentUuid == null
                ? configuracionSistemaRepository.countByClaveAndScope(
                        dto.getClave(), dto.getAplicaA(), dto.getReferenciaUuid())
                : configuracionSistemaRepository.countByClaveAndScopeExcludingUuid(
                        dto.getClave(), dto.getAplicaA(), dto.getReferenciaUuid(), currentUuid);
        if (collisions != null && collisions > 0) {
            throw new IllegalArgumentException(
                    "Ya existe una configuración con la misma clave y alcance");
        }
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
            if (isWholePositiveHoursConfiguration(dto.getClave())
                    && (dto.getValorNumerico() <= 0 || dto.getValorNumerico() % 1 != 0)) {
                throw new IllegalArgumentException(
                        "Las horas de validación de pago deben ser un número entero positivo");
            }
        }
        validateJson(dto.getValorJson(), "valorJson");
        validateJson(dto.getValoresPosibles(), "valoresPosibles");
    }

    private boolean isWholePositiveHoursConfiguration(String clave) {
        return "HORAS_VALIDACION_PAGO".equals(clave)
                || "HORAS_VALIDACION_PAGO_EVENTOS".equals(clave)
                || "HORAS_VALIDACION_PAGO_RECURRENTES".equals(clave);
    }

    private void validateJson(String value, String field) {
        if (value == null || value.trim().isEmpty()) return;
        try {
            objectMapper.readTree(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(field + " debe contener JSON válido");
        }
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
