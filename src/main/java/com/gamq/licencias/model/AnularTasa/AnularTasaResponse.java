package com.gamq.licencias.model.AnularTasa;

import lombok.Data;

@Data
public class AnularTasaResponse {
     private String mensaje;
     private boolean continuarFlujo;
     private String mensajeConfirmacion;
}
