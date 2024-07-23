package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class encargadoComplejoDto {

    private Long idEncargadoComplejo;

    private String uuid;

    private String userUuid;

    private LocalTime horaEntrada;

    private LocalTime horaSalida;

    private String telefono;

    private String nombre;

    private boolean estado;

    private complejoDto complejoDto;

}
