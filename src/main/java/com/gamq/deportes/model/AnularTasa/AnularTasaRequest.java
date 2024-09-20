package com.gamq.deportes.model.AnularTasa;

import lombok.Data;

@Data
public class AnularTasaRequest {
     private String codigoAlcaldia;
     private String codigoUsuario;
     private String numeroTasa;
     private String tipoTasa;
     private String motivoTasa;
     private String observacion;
}
