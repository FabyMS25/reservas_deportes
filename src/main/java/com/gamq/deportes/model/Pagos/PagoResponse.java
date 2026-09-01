package com.gamq.deportes.model.Pagos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class PagoResponse {
    private String mensaje;
    private boolean continuarFlujo;
    private String observacion;
    @JsonAlias("pago")
    private PagoTasa pagoTasa;
}
