package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.canchas;
import com.gamq.licencias.model.contribuyentes;
import com.gamq.licencias.model.pagos;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

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

    private pagosDto pagosDto;

    /*@ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_cancha")
    public canchas cancha;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_contribuyente")
    public contribuyentes contribuyente;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="id_pago")
    public pagos pago;*/

}
