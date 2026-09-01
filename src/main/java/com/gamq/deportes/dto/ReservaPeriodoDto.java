package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaPeriodoDto {
    private String uuid;
    private boolean estado;
    private Date fechaCreacion;

    // Period information
    private Integer mes; // 1-12
    private Integer año;

    // School context
    private String matricula;
    private String disciplina;
    private String entrenador;

    private EstudianteEscuelaDto estudianteEscuelaDto;

    private ReservaDto reservaDto;
}
