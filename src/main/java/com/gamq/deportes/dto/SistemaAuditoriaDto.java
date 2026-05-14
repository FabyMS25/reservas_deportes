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
public class SistemaAuditoriaDto {
    private Long idSistemaAuditoria;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAccion;
    private String usuarioUuid;
    private String accion;
    private String modulo;
    private String entidadTipo;
    private String entidadUuid;
    private String descripcion;
    private String datosCambio;
    private String direccionIp;
    private String userAgent;
}
