package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class TarifaConceptoDto {
    private Long idTarifaConcepto;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String concepto;
    private String tipo; // porcentaje, fijo, por_hora
    private LocalTime desde;
    private LocalTime hasta;
    private BigDecimal valor;
    private Boolean activo = true;

    private TarifaDto tarifaDto;
}
