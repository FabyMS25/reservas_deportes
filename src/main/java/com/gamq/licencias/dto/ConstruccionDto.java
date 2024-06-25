package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.locationtech.jts.geom.Point;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConstruccionDto {

    private Long idConstruccion;

    private String uuid;

    private String codInmueble;

    private String ciTitular;

    private String nombreTitular;

    private String appTitular;

    private String apmTitular;

    private String direccion;

    private Double superficie;

    private String observacion;

    private Double latitud;

    private Double longitud;

    private boolean estado;



}
