package com.gamq.deportes.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class ReservaPuntualDto {
    private Long idReservaPuntual;
    private String uuid;
    private boolean estado;
    private Date fechaCreacion;

    private LocalDate fechaUso;
    private LocalDateTime fechaUsoReal;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Integer duracionHoras;
    private Integer cantidadPersonas;
    private String categoria; //saved to identify later
    private String superficie; //saved to identify later
    private String observaciones;

    private ReservaDto reservaDto;
}
