package com.gamq.deportes.model.AnularTasa;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class AnularTasaRequest {
     private String codigoUsuario;
     private String numeroTasa;
     private String motivoTasa;
     @JsonAlias("observaciones")
     private String observacion;
}
