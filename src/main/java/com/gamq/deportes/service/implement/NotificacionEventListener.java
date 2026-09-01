package com.gamq.deportes.service.implement;

import com.gamq.deportes.event.NotificacionEvent;
import com.gamq.deportes.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificacionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionEventListener.class);
    private final NotificacionService notificacionService;

    public NotificacionEventListener(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true)
    public void procesar(NotificacionEvent event) {
        try {
            switch (event.getOperacion()) {
                case CREAR:
                    notificacionService.crearNotificacion(event.getNotificacion());
                    break;
                case CREAR_ADMIN_SI_HABILITADA:
                    notificacionService.crearNotificacionAdminSiHabilitada(
                            event.getClaveConfiguracion(),
                            event.getNotificacion(),
                            event.getUsuarioExcluidoUuid());
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Operacion de notificacion no soportada: " + event.getOperacion());
            }
        } catch (RuntimeException ex) {
            logger.error(
                    "La operacion principal ya fue confirmada, pero fallo el evento de notificacion {}",
                    event.getOperacion(),
                    ex);
        }
    }
}
