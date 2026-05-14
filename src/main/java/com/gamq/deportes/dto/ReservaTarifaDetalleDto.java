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
public class ReservaTarifaDetalleDto {
    private Long idReservaTarifaDetalle;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String tipoLinea;
    private String nombreConcepto;
    private String tipoCobro;
    private String tipoCalculo;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private String reglaAplicada;
    private String datosSnapshot;

    private ReservaDto reservaDto;
    private TarifaDto tarifaDto;
    private TarifaConceptoDto tarifaConceptoDto;
}
