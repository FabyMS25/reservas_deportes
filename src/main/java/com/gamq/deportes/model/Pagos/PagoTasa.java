package com.gamq.deportes.model.Pagos;

import lombok.Data;

@Data
public class PagoTasa {

    private String numeroTasa;
    private String folio;
    private String formaPago;
    private String fechaPago;
    private String entidadFinanciera;
    private String montoPago;
    private String observacion;

}
