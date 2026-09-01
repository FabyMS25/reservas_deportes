package com.gamq.deportes.model.Pagos;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class PagoTasa {

    private String numeroTasa;
    private String folio;
    private String formaPago;
    private String fechaPago;
    private String entidadFinanciera;
    private String montoPago;
    @JsonAlias({"observaciones", "Observaciones"})
    private String observacion;
    private String estado;

}
