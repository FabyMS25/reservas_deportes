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
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PagoDto {
    private Long idPago;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String numeroTasa;
    private String tipoTasa = "TO";
    private String folio;
    private String formaPago;
    private String fechaPago; // DD/MM/YYYY format
    private String entidadFinanciera;
    private BigDecimal montoPagado;
    private String observaciones;
    private String estadoPago = "pendiente";
    private LocalDateTime ultimaConsulta;
    private String respuestaCompletaRuat;
    private ReservaDto reservaDto;
}
