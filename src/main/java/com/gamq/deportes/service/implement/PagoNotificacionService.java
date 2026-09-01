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

    public PagoNotificacionService(PagoRepository pagoRepository,
                                   NotificacionDestinatariosReservaService destinatariosService,
                                   ApplicationEventPublisher eventPublisher) {
        this.pagoRepository = pagoRepository;
        this.destinatariosService = destinatariosService;
        this.eventPublisher = eventPublisher;
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

        String titulo = "Pago confirmado";
        String mensajeContribuyente = "El pago de tu reserva " + valor(reserva.getNumeroReserva())
                + " fue confirmado." + detallePago(pago, false);
        String mensajeInterno = "El pago de la reserva " + valor(reserva.getNumeroReserva())
                + " de " + valor(reserva.getContribuyente().getNombreCompleto())
                + " fue confirmado." + detallePago(pago, true);
        destinatariosService.publicar(
                NOTIFICAR_PAGO_CONFIRMADO,
                reserva,
                base(pago, titulo, mensajeContribuyente),
                base(pago, titulo, mensajeInterno),
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
                .setAccionUrl(ruta(pago.getReserva()));
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

    private String detallePago(Pago pago, boolean incluirLugar) {
        Reserva reserva = pago.getReserva();
        StringBuilder detalle = new StringBuilder();
        if (reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank()) {
            detalle.append(" Número de tasa: ").append(valor(reserva.getNumeroTasa())).append(".");
        }
        if (reserva.getMontoTotal() != null) {
            detalle.append(" Monto: Bs ").append(reserva.getMontoTotal()).append(".");
        }
        if (incluirLugar && reserva.getCampoDeportivo() != null) {
            detalle.append(" Lugar: ").append(valor(reserva.getCampoDeportivo().getNombre()));
            if (reserva.getCampoDeportivo().getComplejo() != null) {
                detalle.append(", complejo ")
                        .append(valor(reserva.getCampoDeportivo().getComplejo().getNombre()));
            }
            detalle.append(".");
        }
        return detalle.toString();
    }

    private String valor(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
