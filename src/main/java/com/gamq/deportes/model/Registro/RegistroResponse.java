package com.gamq.deportes.model.Registro;

import lombok.Data;

@Data
public class RegistroResponse {
    private String mensaje;
    private boolean continuarFlujo;
    private String codigoContribuyente;

}
