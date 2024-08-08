package com.gamq.licencias.model.Pagos;

import lombok.Data;

@Data
public class PagoToken {
    private String token;
    private PagoRequest pago;
}
