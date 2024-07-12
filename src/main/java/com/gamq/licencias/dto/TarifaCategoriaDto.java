package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.Categoria;
import com.gamq.licencias.model.Tarifas;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class TarifaCategoriaDto {
    private String uuid;

    private boolean estado;

    public TarifaDto tarifaDto;

    public CategoriaDto categoriaDto;
}
