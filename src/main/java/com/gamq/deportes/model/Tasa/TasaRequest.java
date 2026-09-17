package com.gamq.deportes.model.Tasa;

import lombok.Data;

@Data
public class TasaRequest {

        private String codigoUsuario;
        private String codigoContribuyente;
        private String servicioMunicipal;
        private DatosConcepto datosConcepto[];
        private String observacion;

}
