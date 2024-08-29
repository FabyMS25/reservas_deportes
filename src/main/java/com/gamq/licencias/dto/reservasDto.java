package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.pagos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class reservasDto {
    private Long idReserva;

    private String uuid;

    private Date fechaCreated;

    private Date fechaReserva;

    private LocalTime horaEntrada;

    private LocalTime horaSalida;

    private BigDecimal totalPago;

    private String categoria;

    private String superficie;

    private int totalPersonas;

    private boolean estado;

    private canchasDto canchasDto;

    private contribuyenteDto contribuyenteDto;

    private String numeroTasa;

    private String estadoReserva;

    public List<pagosDto> pagosDtoList = new ArrayList<>();

}
