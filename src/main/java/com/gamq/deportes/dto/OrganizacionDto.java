package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.deportes.model.Contribuyente;
import com.gamq.deportes.model.Organizacion;
import com.gamq.deportes.model.Reserva;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.LocalDate;
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

    private String tipoOrganizacion; // asociacion_deportiva, club_deportivo, unidad_educativa, empresa_privada, institucion_publica
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private String personeriaJuridica;
    private String nroNit;
    private Boolean activo;

    private ContribuyenteDto representanteDto;
    private List<ReservaDto> reservasDtoList = new ArrayList<>();
}
