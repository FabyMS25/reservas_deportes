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
public class CampoDeportivoDto {
    private Long idCampo;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;


//    private String Tipo;

    private String nombre;
    private String codigo;
    //    private String tipoEstructura;
    private String dimensiones;
    private Integer capacidadPersonas = 0;
    private Boolean tieneIluminacion = false;
//    private Boolean tieneVestuarios = false;
//    private Boolean tieneGraderias = false;
    private String estadoActual;
    private String observaciones;

    private ComplejoDeportivoDto complejoDto;
    private SuperficieDto superficieDto;

    private List<TarifaDto> tarifasDtoList = new ArrayList<>();
    private List<HorarioFijoDto> horariosFijosDtoList = new ArrayList<>();
    private List<HorarioExcepcionDto> horariosExcepcionesDtoList = new ArrayList<>();
//    private List<CampoDisciplinaDto> disciplinasDtoList = new ArrayList<>();

    public boolean isActivo() {
        return "activo".equals(estadoActual) && !estado;
    }
    //public List<ReservasProgramadasDto> reservasProgramadasDtosListDto = new ArrayList<>();

}
