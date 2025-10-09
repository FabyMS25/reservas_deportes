package com.gamq.deportes.model.Contribuyentes;
import lombok.Data;

@Data
public class ContribuyenteResponse {

    private String mensaje;
    private boolean continuarFlujo;
    private String codigoContribuyente;
    private Cliente contribuyente;
}
