package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TarifaDto {
    private Long idTarifa;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private BigDecimal precioBase;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalTime desde;
    private LocalTime hasta;
    private Boolean activo = true;
    private String descripcion;
    private String observaciones;
    @Column(name = "tipo_cobro", length = 20)
    @Pattern(regexp = "por_hora|por_persona|por_evento|por_sesion|mensualidad|por_club", message = "Tipo de cobro no válido")
    private String tipoCobro; // 'por_hora' | 'por_persona' | 'por_evento' | 'por_sesion' | 'mensualidad' | 'por_club';
    private String frecuencia; // 'diaria' | 'semanal' | 'mensual'; // For recurring calculations
    private BigDecimal sesionesIncluidas; // number; // For 'mensualidad' or 'por_club' types

    private CampoDeportivoDto campoDeportivoDto;
    private ClasificadorRuatDto clasificadorRuatDto;

    private List<TarifaCategoriaDto> categoriasDtoList = new ArrayList<>();
    private List<TarifaConceptoDto> conceptosDtos = new ArrayList<>();
}
