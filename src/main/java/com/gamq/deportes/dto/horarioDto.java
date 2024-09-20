package com.gamq.deportes.dto;

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
public class horarioDto {

    private Long idHorario;

    private String uuid;

    private String dia;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    private boolean abierto;

    private boolean estado;

    private canchasDto canchasDto;

    /*@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;*/

}
