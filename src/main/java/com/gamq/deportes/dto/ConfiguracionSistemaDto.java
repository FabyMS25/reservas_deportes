package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfiguracionSistemaDto {
    private Long idConfiguracion;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String clave;              // Unique key identifier
    private String categoria;          // Group related configs

    private Double valorNumerico;      // For numeric values
    private String valorTexto;         // For text values
    private Boolean valorBoolean;      // For boolean flags
    private String valorJson;          // For complex objects (JSON string)

    private String nombre;             // Display name
    private String descripcion;        // What this config does
    private String unidadMedida;       // 'horas', 'dias', 'bolivianos', 'porcentaje'

    private Double valorMinimo;        // Min allowed value
    private Double valorMaximo;        // Max allowed value
    private String valoresPosibles;    // JSON array of allowed values

    // Control
    private Boolean modificablePorUsuario = false;
    private Boolean requiereAprobacion = false;
    private Boolean activo = true;
    private LocalDateTime ultimaModificacion;
    private String modificadoPor;      // User UUID who modified
    private String aplicaA;            // 'global', 'campo_deportivo', 'complejo', 'tarifa'
    private String referenciaUuid;     // UUID of related entity (if aplicaA != 'global')

    public String getValorComoString() {
        if (valorTexto != null) return valorTexto;
        if (valorNumerico != null) return String.valueOf(valorNumerico);
        if (valorBoolean != null) return String.valueOf(valorBoolean);
        if (valorJson != null) return valorJson;
        return null;
    }

    public Double getValorComoDouble() {
        if (valorNumerico != null) return valorNumerico;
        if (valorTexto != null) {
            try {
                return Double.parseDouble(valorTexto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    public Boolean getValorComoBoolean() {
        if (valorBoolean != null) return valorBoolean;
        if (valorTexto != null) {
            return Boolean.parseBoolean(valorTexto);
        }
        return null;
    }

    public boolean isActivo() {
        return Boolean.TRUE.equals(activo) && !estado;
    }
}