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

public class ClasificadorRuatDto {
    private Long idClasificadorRuat;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;

    private String codigo;
    private String nombre;
    private String descripcion;
//    private String tipoReserva; // por_hora, por_persona, por_evento, por_club , mensualidad
    private String tipoArancel = "DI"; // DI = Discrecional
    private Boolean activo = true;

    private List<TarifaDto> tarifasDtoList = new ArrayList<>();
}
