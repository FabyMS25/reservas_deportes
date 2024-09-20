package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
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
    private String uuid;

    private String descripcion;

    private LocalTime desde;

    private LocalTime hasta;

    private BigDecimal costo;

    private canchasDto canchasDto;

    private boolean estado;

    public List<TarifaCategoriaDto> categoriaDtoList = new ArrayList<>();

}
