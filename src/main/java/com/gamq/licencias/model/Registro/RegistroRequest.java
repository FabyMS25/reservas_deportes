package com.gamq.licencias.model.Registro;

import lombok.Data;

@Data
public class RegistroRequest {

    private String codigoAlcaldia;
    private String codigoUsuario;
    private String numeroDocumento;
    private String tipoDocumento;
    private String expedido;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String estadoCivil;
    private String fechaNacimiento;
    private String genero;
    private String motivo;
    private String observacion;

}
