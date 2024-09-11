package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class complejoDto {


    private String uuid;

    private String nombre;

    private String techado;

    private String ubicacion;

    private String distrito;

    private Double latitud;

    private Double longitud;

    private boolean estado;


    public List<encargadoComplejoDto> encargadosDtoList = new ArrayList<>();

}
