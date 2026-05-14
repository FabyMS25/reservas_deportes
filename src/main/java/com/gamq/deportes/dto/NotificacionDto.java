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
public class NotificacionDto {
    private Long idNotificacion;
    private String uuid;
    private boolean estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    private String usuarioUuid;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String estadoLectura;
    private String prioridad;
    private String canal;
    private String referenciaTipo;
    private String referenciaUuid;
    private String accionUrl;
    private LocalDateTime programadaPara;
    private LocalDateTime enviadaEn;
    private LocalDateTime leidaEn;
    private LocalDateTime expiraEn;
    private String metadataJson;
}
