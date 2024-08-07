package com.gamq.licencias.model.Contribuyente;
import lombok.Data;


@Data
public class ContribuyenteRequest {


       private String codigoAlcaldia;
       private String numeroDocumento;
       private String tipoDocumento;
       private String expedido;

}
