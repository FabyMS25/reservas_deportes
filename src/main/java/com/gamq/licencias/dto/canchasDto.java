package com.gamq.licencias.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamq.licencias.model.*;
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

    private String codigo;

    private String Tipo;

    private boolean estado;

    public complejoDto complejoDto;

    public SuperficieDto superficieDto;

    //@OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
   // public List<encargadoComplejo> encargadoComplejoList = new ArrayList<>();

    //@OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)

    public List<TarifaDto> tarifasDtoList = new ArrayList<>();

    public List<horarioDto> horarioDtoList = new ArrayList<>();


    //public List<horarioDto> horarioListDto = new ArrayList<>();


    //@OneToMany(mappedBy = "cancha", cascade = CascadeType.ALL)
    //public List<reservas> reservasList = new ArrayList<>();


}
