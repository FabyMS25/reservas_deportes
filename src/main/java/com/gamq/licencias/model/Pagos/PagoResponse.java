package com.gamq.licencias.model.Pagos;

import lombok.Data;

@Data
public class PagoResponse {
    private String mensaje;
    private String continuarFlujo;
    private String observacion;
    private PagoTasa pagoTasa;
}
