package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.canchas;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class encargadoCanchaDto {

    private Long idEncargadoCancha;

    private String uuid;

    private String userUuid;

    private LocalTime horaEntrada;

    private LocalTime horaSalida;


    /*@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;*/
}
