package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.event.PagoConfirmadoEvent;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.model.Pago;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.repository.PagoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.gamq.deportes.exception.enums.EntityType.PAGOS;

@Service
public class PagoNotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(PagoNotificacionService.class);
    private static final String NOTIFICAR_PAGO_CONFIRMADO = "NOTIFICAR_PAGO_CONFIRMADO";

    private final PagoRepository pagoRepository;
    private final NotificacionDestinatariosReservaService destinatariosService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionOperativaService notificacionOperativaService;

    public PagoNotificacionService(PagoRepository pagoRepository,
                                   NotificacionDestinatariosReservaService destinatariosService,
                                   ApplicationEventPublisher eventPublisher,
                                   NotificacionOperativaService notificacionOperativaService) {
        this.pagoRepository = pagoRepository;
        this.destinatariosService = destinatariosService;
        this.eventPublisher = eventPublisher;
        this.notificacionOperativaService = notificacionOperativaService;
    }

    public void notificarDespuesDeConfirmar(String pagoUuid) {
        eventPublisher.publishEvent(new PagoConfirmadoEvent(pagoUuid));
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void alConfirmarPago(PagoConfirmadoEvent event) {
        try {
            preparar(event.getPagoUuid());
        } catch (RuntimeException ex) {
            logger.error(
                    "El pago {} ya fue confirmado, pero fallo la preparacion de sus notificaciones",
                    event.getPagoUuid(),
                    ex);
            notificacionOperativaService.error("Notificación de pago confirmado", ex.getMessage());
        }
    }

    private void preparar(String pagoUuid) {
        Pago pago = pagoRepository.findByUuid(pagoUuid)
                .orElseThrow(() -> new NotFoundException(PAGOS, pagoUuid));
        Reserva reserva = pago.getReserva();
        if (reserva == null || reserva.getContribuyente() == null
                || reserva.getContribuyente().getUuid() == null) {
            return;
        }

        String estadoPago = pago.getEstadoPago();
        if (!"confirmado".equalsIgnoreCase(estadoPago)
                && !"pagado".equalsIgnoreCase(estadoPago)) {
            return;
        }

        String titulo = ReservaNotificacionFormatter.titulo("Pago confirmado", reserva);
        String mensaje = ReservaNotificacionFormatter.mensaje(
                reserva, "El pago fue confirmado" + detallePago(pago));
        NotificacionDto comun = base(pago, titulo, mensaje);
        destinatariosService.publicar(
                NOTIFICAR_PAGO_CONFIRMADO,
                reserva,
                comun,
                comun,
                null);
    }

    private NotificacionDto base(Pago pago, String titulo, String mensaje) {
        return new NotificacionDto()
                .setTipo(NOTIFICAR_PAGO_CONFIRMADO)
                .setTitulo(titulo)
                .setMensaje(mensaje)
                .setPrioridad("SUCCESS")
                .setCanal("APP")
                .setReferenciaTipo("PAGO")
                .setReferenciaUuid(pago.getUuid())
                .setAccionUrl(ReservaNotificacionFormatter.ruta(pago.getReserva()));
    }

    private String ruta(Reserva reserva) {
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/recurrent";
        }
        if ("periodo".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/schools";
        }
        return "/reservations/one-time";
    }

    private String detallePago(Pago pago) {
        Reserva reserva = pago.getReserva();
        StringBuilder detalle = new StringBuilder();
        if (reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank()) {
            detalle.append(" Número de tasa: ").append(valor(reserva.getNumeroTasa())).append(".");
        }
        return detalle.toString();
    }

    private String valor(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
