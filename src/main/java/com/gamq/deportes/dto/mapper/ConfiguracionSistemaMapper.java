package com.gamq.deportes.dto.mapper;

import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import com.gamq.deportes.model.ConfiguracionSistema;

public class ConfiguracionSistemaMapper {

    public static ConfiguracionSistemaDto toDto(ConfiguracionSistema configuracion) {
        ConfiguracionSistemaDto dto = new ConfiguracionSistemaDto();

        dto.setIdConfiguracion(configuracion.getIdConfiguracion());
        dto.setUuid(configuracion.getUuid());
        dto.setEstado(configuracion.isEstado());
        dto.setFechaCreacion(configuracion.getFechaCreacion());
        dto.setFechaActualizacion(configuracion.getFechaActualizacion());

        // Identification
        dto.setClave(configuracion.getClave());
        dto.setCategoria(configuracion.getCategoria());

        // Values (flexible storage)
        dto.setValorNumerico(configuracion.getValorNumerico());
        dto.setValorTexto(configuracion.getValorTexto());
        dto.setValorBoolean(configuracion.getValorBoolean());
        dto.setValorJson(configuracion.getValorJson());

        // Metadata
        dto.setNombre(configuracion.getNombre());
        dto.setDescripcion(configuracion.getDescripcion());
        dto.setUnidadMedida(configuracion.getUnidadMedida());

        // Validation
        dto.setValorMinimo(configuracion.getValorMinimo());
        dto.setValorMaximo(configuracion.getValorMaximo());
        dto.setValoresPosibles(configuracion.getValoresPosibles());

        // Control
        dto.setModificablePorUsuario(configuracion.getModificablePorUsuario());
        dto.setRequiereAprobacion(configuracion.getRequiereAprobacion());
        dto.setActivo(configuracion.getActivo());

        // Audit
        dto.setUltimaModificacion(configuracion.getUltimaModificacion());
        dto.setModificadoPor(configuracion.getModificadoPor());

        // Application scope
        dto.setAplicaA(configuracion.getAplicaA());
        dto.setReferenciaUuid(configuracion.getReferenciaUuid());

        return dto;
    }

    public static ConfiguracionSistema toEntity(ConfiguracionSistemaDto dto) {
        ConfiguracionSistema configuracion = new ConfiguracionSistema();

        configuracion.setUuid(dto.getUuid());
        configuracion.setEstado(dto.isEstado());
        configuracion.setFechaCreacion(dto.getFechaCreacion());
        configuracion.setFechaActualizacion(dto.getFechaActualizacion());

        return updateEntityFromDto(dto, configuracion);
    }

    public static ConfiguracionSistema updateEntityFromDto(ConfiguracionSistemaDto dto, ConfiguracionSistema configuracion) {
        // Identification
        configuracion.setClave(dto.getClave());
        configuracion.setCategoria(dto.getCategoria());

        // Values (flexible storage)
        configuracion.setValorNumerico(dto.getValorNumerico());
        configuracion.setValorTexto(dto.getValorTexto());
        configuracion.setValorBoolean(dto.getValorBoolean());
        configuracion.setValorJson(dto.getValorJson());

        // Metadata
        configuracion.setNombre(dto.getNombre());
        configuracion.setDescripcion(dto.getDescripcion());
        configuracion.setUnidadMedida(dto.getUnidadMedida());

        // Validation
        configuracion.setValorMinimo(dto.getValorMinimo());
        configuracion.setValorMaximo(dto.getValorMaximo());
        configuracion.setValoresPosibles(dto.getValoresPosibles());

        // Control
        configuracion.setModificablePorUsuario(dto.getModificablePorUsuario());
        configuracion.setRequiereAprobacion(dto.getRequiereAprobacion());
        configuracion.setActivo(dto.getActivo());

        // Audit
        configuracion.setUltimaModificacion(dto.getUltimaModificacion());
        configuracion.setModificadoPor(dto.getModificadoPor());

        // Application scope
        configuracion.setAplicaA(dto.getAplicaA());
        configuracion.setReferenciaUuid(dto.getReferenciaUuid());

        return configuracion;
    }

    // Simple DTO for basic operations
    public static ConfiguracionSistemaDto toSimpleDto(ConfiguracionSistema configuracion) {
        ConfiguracionSistemaDto dto = new ConfiguracionSistemaDto();

        dto.setIdConfiguracion(configuracion.getIdConfiguracion());
        dto.setUuid(configuracion.getUuid());
        dto.setClave(configuracion.getClave());
        dto.setCategoria(configuracion.getCategoria());
        dto.setNombre(configuracion.getNombre());
        dto.setActivo(configuracion.getActivo());
        dto.setAplicaA(configuracion.getAplicaA());

        return dto;
    }
}