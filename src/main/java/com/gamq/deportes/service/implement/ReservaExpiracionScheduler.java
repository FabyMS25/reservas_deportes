package com.gamq.deportes.service.implement;

import com.gamq.deportes.service.ReservaPuntualService;
import com.gamq.deportes.service.ReservaRecurrenteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReservaExpiracionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservaExpiracionScheduler.class);

    private final ReservaPuntualService reservaPuntualService;
    private final ReservaRecurrenteService reservaRecurrenteService;
    private final ReservaValidacionExpiracionService validacionExpiracionService;

    public ReservaExpiracionScheduler(ReservaPuntualService reservaPuntualService,
                                      ReservaRecurrenteService reservaRecurrenteService,
                                      ReservaValidacionExpiracionService validacionExpiracionService) {
        this.reservaPuntualService = reservaPuntualService;
        this.reservaRecurrenteService = reservaRecurrenteService;
        this.validacionExpiracionService = validacionExpiracionService;
    }

    @Scheduled(fixedDelayString = "${gamq.reservas.expiracion.fixed-delay-ms:60000}")
    public void procesarExpiraciones() {
        ejecutarAislado("puntuales", reservaPuntualService::checkAndUpdateExpiredReservations);
        ejecutarAislado("recurrentes", reservaRecurrenteService::checkAndUpdateExpiredReservations);
        ejecutarAislado("solicitudes sin validacion", validacionExpiracionService::procesar);
    }

    private void ejecutarAislado(String tipo, Runnable tarea) {
        try {
            tarea.run();
        } catch (RuntimeException ex) {
            logger.error("No se pudieron procesar las reservas {} expiradas", tipo, ex);
        }
    }
}
