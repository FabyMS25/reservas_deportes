package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizacionDto {
    private Long idOrganizacion;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private Boolean activo;

    private String tipoOrganizacion;
    private String personeriaJuridica;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private String nroNit;

    private String entrenador;
    private String disciplina;
    private String codigoEscuela;

    private Boolean requierePagoMensualidad;
    private int tarifaMensual;
    private Boolean permiteReservasGratuitas;

    private List<OrganizacionRepresentanteDto> representantesDto;
    private List<ReservaDto> reservasDtoList = new ArrayList<>();
}
