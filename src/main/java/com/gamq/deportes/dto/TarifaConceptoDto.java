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
    private String tipo; // porcentaje, fijo
    private BigDecimal valor;
    private Boolean activo = true;

    private TarifaDto tarifaDto;
}
