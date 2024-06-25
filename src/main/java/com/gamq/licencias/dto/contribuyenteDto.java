package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class contribuyenteDto {

    private Long idContribuyente;

    private String uuid;

    private String userName;

    private String password;

    private String nombre;

    private String apPaterno;

    private String apMaterno;

    private String nroCarnet;

    private String nroNit;

    private String ciExpedido;

    private String sexo;

    private String direccion;

    private String celular;

    private String correo;

    private boolean estado;


    /*@OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL)
    public List<reservas> reservasList = new ArrayList<>();*/

}
