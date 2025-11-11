package com.gamq.deportes.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)

public class ReservaRecurrenteDto {
    private Long idReservaRecurrente;
    private String uuid;
    private boolean estado;
    private Date fechaCreacion;

    private String tipoRecurrencia; //menusal / semanal
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer cantidadPersonas;
    private String diasSemana;
    private Integer cantidadSesiones;
    private Integer duracionSesionHoras;
    private BigDecimal tarifaMensual;
    private Boolean activo;
//    private String horarios; // JSON string or you can create a separate HorarioDto
    private String categoria; //saved to identify later
    private String superficie; //saved to identify later
    private String observaciones;

    public TableroDto tableroDto;
    private ReservaDto reservaDto;
}
