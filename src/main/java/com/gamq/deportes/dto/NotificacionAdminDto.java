package com.gamq.deportes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class NotificacionAdminDto {
    private String uuid;
    private LocalDateTime fechaCreacion;
    private String tipo;
    private String titulo;
    private String mensaje;
    private String prioridad;
    private String canal;
    private String referenciaTipo;
    private String referenciaUuid;
    private String accionUrl;
    private String metadataJson;
    private String usuarioExcluidoUuid;
}
