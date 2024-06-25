package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.reservas;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
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
public class pagosDto {

    private Long idPago;

    private String uuid;

    private Long montoPago;

    private Date fechaPago;

    private String nroTasa;

    private String estadoPago;

    private boolean estado;

    //@OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    //public List<reservas> reservasList = new ArrayList<>();

}
