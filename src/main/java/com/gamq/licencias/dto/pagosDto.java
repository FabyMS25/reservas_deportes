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
import java.math.BigDecimal;
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

    private Date fechaPago;

    private BigDecimal montoPago;

    private String numeroTasa;

    private String estadoPago;

    private String entidadFinanciera;

    private String folio;

    private String  formaPago;

    private  String observacion;

    private boolean estado;

    private reservasDto reservasDto;
    //@OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    //public List<reservas> reservasList = new ArrayList<>();

}
