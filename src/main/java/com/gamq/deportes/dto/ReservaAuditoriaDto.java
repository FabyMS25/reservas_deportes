package com.gamq.deportes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReservaAuditoriaDto {

    private Long idReservaAuditoria;
    private String uuid;
    private boolean estado;

    private String accion;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioUuid;
    private String datosCambio; // JSON string
    private String observaciones;
    private String direccionIp;
    private LocalDateTime fechaAccion;
    private ReservaDto reservaDto;
}
