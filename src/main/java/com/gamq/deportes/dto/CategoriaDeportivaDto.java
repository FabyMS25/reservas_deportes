package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoriaDeportivaDto {
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String codigo;
    private String nombre;
    private String descripcion;
    private boolean activo = true;

    private Boolean requiereAprobacion = false;
    private Integer maxDuracionAutoAprobacion;
    private Integer maxPersonasAutoAprobacion;
        private String tipo;
        private boolean forAsociacion;


    public boolean requiereAprobacionReserva(ReservaDto reservaDto) {
        if (!Boolean.TRUE.equals(this.requiereAprobacion)) {
            return false;
        }
        return !esReservaAutoAprobable(reservaDto);
    }

    public boolean esReservaAutoAprobable(ReservaDto reservaDto) {
        if (maxPersonasAutoAprobacion != null && reservaDto.getReservaPuntualDto() != null) {
            Integer cantidadPersonas = reservaDto.getReservaPuntualDto().getCantidadPersonas();
            if (cantidadPersonas != null && cantidadPersonas <= maxPersonasAutoAprobacion) {
                return true;
            }
        }
        if (maxDuracionAutoAprobacion != null && reservaDto.getReservaPuntualDto() != null) {
            Integer duracion = reservaDto.getReservaPuntualDto().getDuracionHoras();
            if (duracion != null && duracion <= maxDuracionAutoAprobacion) {
                return true;
            }
        }
        return false;
    }
}
