package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ReservaBorderouxActividadDto {

    private Long idReservaBorderouxActividad;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private LocalDate fechaActividad;
    private BigDecimal porcentajeAplicado;
    private BigDecimal subtotal;
    private String observaciones;

    private CampoDeportivoDto campoDeportivoDto;
    private CategoriaDeportivaDto categoriaDeportivaDto;
    private List<ReservaBorderouxTalonarioDto> talonariosDtoList = new ArrayList<>();

    private ReservaDto reservaDto;
}
