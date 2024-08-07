package com.gamq.licencias.model.Tasa;

import lombok.Data;

@Data
public class TasaRequest {

        private String codigoAlcaldia;
        private String codigoUsuario;
        private String codigoContribuyente;
        private String servicioMunicipal;
        private DatosConcepto datosConcepto[];
        private String observacion;

}
