package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/** Emits deduplicated administrator-only alerts without affecting the failed business transaction. */
@Service
public class NotificacionOperativaService {

    public static final String NOTIFICAR_ERROR_OPERATIVO = "NOTIFICAR_ERROR_OPERATIVO";
    private final NotificacionEventPublisher eventPublisher;

    public NotificacionOperativaService(NotificacionEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void error(String proceso, String detalle) {
        String periodo = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).toString();
        String referencia = UUID.nameUUIDFromBytes(
                (proceso + "|" + periodo).getBytes(StandardCharsets.UTF_8)).toString();
        NotificacionDto alerta = new NotificacionDto()
                .setTipo(NOTIFICAR_ERROR_OPERATIVO)
                .setTitulo("Error operativo · " + proceso)
                .setMensaje("Un proceso interno requiere revisión. Proceso: " + proceso
                        + ". Detalle: " + detalleSeguro(detalle) + ".")
                .setPrioridad("ERROR")
                .setCanal("APP")
                .setReferenciaTipo("SISTEMA")
                .setReferenciaUuid(referencia)
                .setAccionUrl("/settings/system-configurations")
                .setMetadataJson("{\"proceso\":\"" + json(proceso) + "\",\"periodo\":\"" + periodo + "\"}");
        eventPublisher.publicarAdminSiHabilitada(NOTIFICAR_ERROR_OPERATIVO, alerta, null);
    }

    private String detalleSeguro(String detalle) {
        if (detalle == null || detalle.isBlank()) return "Sin detalle disponible";
        String limpio = detalle.replaceAll("[\\r\\n]+", " ").trim();
        return limpio.length() > 240 ? limpio.substring(0, 240) + "…" : limpio;
    }

    private String json(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
