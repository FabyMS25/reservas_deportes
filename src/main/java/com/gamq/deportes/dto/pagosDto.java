package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Date;

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
