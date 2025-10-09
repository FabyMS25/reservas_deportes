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
public class EncargadoComplejoDto {
    private Long idEncargadoComplejo;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String userUuid;
    private String avatar;
    private String nombre;
    private String telefono;
    private Boolean activo = true;
    private LocalDate fechaAsignacion;
    private LocalDate fechaFinAsignacion;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    private ComplejoDeportivoDto complejoDto;
    public boolean isActualmenteActivo() {
        LocalDate today = LocalDate.now();
        return activo &&
                (fechaFinAsignacion == null || today.isBefore(fechaFinAsignacion) || today.isEqual(fechaFinAsignacion));
    }
}
