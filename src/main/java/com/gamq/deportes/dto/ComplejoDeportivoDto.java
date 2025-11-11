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
public class ComplejoDeportivoDto {
    private Long idComplejo;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String estadoActual; // activo, mantenimiento, cerrado
    private String nombre;
    private String telefonoPrincipal;
    private String tipoEstructura; // techado, abierto, cerrado
    private boolean tieneIluminacion;
    private boolean tieneVestuarios;
    private boolean tieneGraderias;
    private String observaciones;

    private String distrito;
    private String ubicacion;
    private Double latitud;
    private Double longitud;

    private List<EncargadoComplejoDto> encargadosDtoList = new ArrayList<>();
    private List<CampoDeportivoDto> camposDeportivosDtoList = new ArrayList<>();
    public boolean hasGeoLocation() {
        return latitud != null && longitud != null;
    }
}
