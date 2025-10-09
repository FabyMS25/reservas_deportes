package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorarioFijoDto {
    private Long idHorarioFijo;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String dia;
    private boolean abierto;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private String observaciones;

    private CampoDeportivoDto campoDeportivoDto;

    public boolean isHorarioValido() {
        return horaApertura != null && horaCierre != null && horaApertura.isBefore(horaCierre);
    }

    public String getHorarioFormateado() {
        if (horaApertura != null && horaCierre != null) {
            return horaApertura.toString() + " - " + horaCierre.toString();
        }
        return "Horario no definido";
    }
}
