package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.canchaDeportes;
import com.gamq.licencias.model.encargadoCancha;
import com.gamq.licencias.model.horario;
import com.gamq.licencias.model.reservas;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
public class canchasDto {
    private Long idCancha;

    private String uuid;

    private String techado;

    private String superficie;

    private String ubicacion;

    private String distrito;

    private Double totalxhora;

    private Double latitud;

    private Double longitud;

    private String nombre;

    private boolean estado;

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<canchaDeportes> canchaDeportesList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<encargadoCancha> encargadoCanchaList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<horario> horarioList = new ArrayList<>();

    @OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    public List<reservas> reservasList = new ArrayList<>();


}
