package com.gamq.deportes.model.Contribuyente;
import lombok.Data;

@Data
public class ContribuyenteResponse {

    private String mensaje;
    private boolean continuarFlujo;
    private String codigoContribuyente;
    private Cliente contribuyente;
}
