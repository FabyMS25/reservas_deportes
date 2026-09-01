package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.event.NotificacionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NotificacionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public NotificacionEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publicar(NotificacionDto notificacion) {
        eventPublisher.publishEvent(NotificacionEvent.crear(notificacion));
    }

    public void publicarAdminSiHabilitada(String claveConfiguracion,
                                          NotificacionDto notificacion,
                                          String usuarioExcluidoUuid) {
        eventPublisher.publishEvent(NotificacionEvent.crearAdminSiHabilitada(
                claveConfiguracion, notificacion, usuarioExcluidoUuid));
    }
}
