package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HorarioExcepcionDto {
    private Long idHorarioExcepcion;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private LocalDate fecha;
    private String tipoExcepcion;
    private LocalTime horaApertura;
    private LocalTime horaCierre;
    private String motivo;

    private CampoDeportivoDto campoDeportivoDto;

    public String getHorarioFormateado() {
        if (isCerrado()) {
            return "CERRADO";
        }
        if (horaApertura != null && horaCierre != null) {
            return horaApertura.toString() + " - " + horaCierre.toString();
        }
        return "Horario especial";
    }
    public boolean isCerrado() {
        return "cerrado".equals(tipoExcepcion);
    }
    public boolean isHorarioEspecial() {
        return "horario_especial".equals(tipoExcepcion);
    }
    public boolean isMantenimiento() {
        return "mantenimiento".equals(tipoExcepcion);
    }
    public boolean isEventoMunicipal() {
        return "evento_municipal".equals(tipoExcepcion);
    }
}
