package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaBorderouxTalonarioDto {

    private Long idReservaBorderouxTalonario;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private BigDecimal precioBoleta;
    private Integer numeroInicial;
    private Integer numeroFinal;
    private String serieTalonario;
    private Integer cantidad;
    private Integer cantidadAnuladas;
    private Integer cantidadVendida;
    private BigDecimal totalBoletas;
    private BigDecimal montoBorderoux;
}
