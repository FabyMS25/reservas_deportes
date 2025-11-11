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

    private String estadoActual;
    private String nombre;
    private String codigo;
    private String categoria;
    //    private String tipoEstructura;
    private String dimensiones;
    private Integer capacidadPersonas = 0;
    private Boolean tieneIluminacion = false;
//    private Boolean tieneVestuarios = false;
//    private Boolean tieneGraderias = false;
    private Boolean accesoLibre = false;
    private String observaciones;


    private Boolean requiereAprobacion = false;
    private Integer horasMinimaAprobacion;
    private Integer maxDuracionAutoAprobacion;
    private Integer maxPersonasAutoAprobacion;


    private ComplejoDeportivoDto complejoDto;
    private SuperficieDto superficieDto;

    private List<TarifaDto> tarifasDtoList = new ArrayList<>();
    private List<HorarioFijoDto> horariosFijosDtoList = new ArrayList<>();
    private List<HorarioExcepcionDto> horariosExcepcionesDtoList = new ArrayList<>();
//    private List<CampoDisciplinaDto> disciplinasDtoList = new ArrayList<>();

    public boolean isActivo() {
        return "activo".equals(estadoActual) && !estado;
    }


    public boolean requiereAprobacionReserva(ReservaDto reservaDto) {
        if (!this.requiereAprobacion) {
            return false;
        }
        if (esReservaAutoAprobable(reservaDto)) {
            return false;
        }
        return true;
    }
    public boolean esReservaAutoAprobable(ReservaDto reservaDto) {
        if (horasMinimaAprobacion != null) {
            LocalDateTime ahora = LocalDateTime.now();
            long horasHastaReserva = java.time.Duration.between(ahora, reservaDto.getFechaReserva()).toHours();
            if (horasHastaReserva >= horasMinimaAprobacion) {
                return true;
            }
        }
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
