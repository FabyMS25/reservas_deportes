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

public class ReservasProgramadasDto {
    private String uuid;
    private Date fechaCreated;
    private Date fechaReserva;

    private String categoria;
    private String superficie;
    private int totalPersonas;
    private String numeroTasa;

    private String estadoReserva;
    private boolean estado;

    public CampoDeportivoDto campoDeportivoDto;

    public contribuyenteDto contribuyenteDto;

    public TableroDto tableroDto;

}
