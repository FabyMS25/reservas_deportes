package com.gamq.licencias.model.Contribuyente;

import lombok.Data;

@Data
public class Cliente {
    private String tipoContribuyente;
    private String numeroDocumento;
    private String tipoDocumento;
    private String expedido;
    private String nombre;
    private String primerApellido;
    private String segundoApellido;
    private String estadoCivil;
    private String fechaNacimiento;
    private String genero;
    private String apellidoEsposo;
    private DatosDireccion datosDireccion;

}
