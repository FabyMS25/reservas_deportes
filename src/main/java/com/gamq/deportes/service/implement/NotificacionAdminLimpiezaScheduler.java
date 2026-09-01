package com.gamq.deportes.service.implement;

import com.gamq.deportes.service.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificacionAdminLimpiezaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionAdminLimpiezaScheduler.class);
    private static final int DIAS_RETENCION = 30;
    private final NotificacionService notificacionService;

    public NotificacionAdminLimpiezaScheduler(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @Scheduled(cron = "${gamq.notificaciones.admin.limpieza-cron:0 15 2 * * *}")
    public void limpiar() {
        try {
            int archivadas = notificacionService.limpiarNotificacionesAdminAntiguas(DIAS_RETENCION);
            if (archivadas > 0) logger.info("Se archivaron {} notificaciones administrativas antiguas", archivadas);
        } catch (RuntimeException ex) {
            logger.error("No se pudieron limpiar las notificaciones administrativas antiguas", ex);
        }
    }
}
