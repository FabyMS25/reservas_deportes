package com.gamq.deportes.model.Contribuyentes;
import lombok.Data;


@Data
public class ContribuyenteRequest {


       private String codigoAlcaldia;
       private String numeroDocumento;
       private String tipoDocumento;
       private String expedido;

}
