package com.gamq.deportes.service.implement;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.event.ReservaNotificacionEvent;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.gamq.deportes.exception.enums.EntityType.RESERVA;

@Service
public class ReservaNotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(ReservaNotificacionService.class);
    private static final String NOTIFICAR_RESERVA_ANULADA = "NOTIFICAR_RESERVA_ANULADA";
    private static final String NOTIFICAR_RESERVA_EXPIRADA = "NOTIFICAR_RESERVA_EXPIRADA";
    private final ReservaRepository reservaRepository;
    private final NotificacionDestinatariosReservaService destinatariosService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificacionOperativaService notificacionOperativaService;

    public ReservaNotificacionService(ReservaRepository reservaRepository,
                                      NotificacionDestinatariosReservaService destinatariosService,
                                      ApplicationEventPublisher eventPublisher,
                                      NotificacionOperativaService notificacionOperativaService) {
        this.reservaRepository = reservaRepository;
        this.destinatariosService = destinatariosService;
        this.eventPublisher = eventPublisher;
        this.notificacionOperativaService = notificacionOperativaService;
    }

    public void notificarCancelada(Reserva reserva) {
        eventPublisher.publishEvent(new ReservaNotificacionEvent(
                reserva.getUuid(), ReservaNotificacionEvent.Tipo.CANCELADA));
    }

    public void notificarExpirada(Reserva reserva) {
        eventPublisher.publishEvent(new ReservaNotificacionEvent(
                reserva.getUuid(), ReservaNotificacionEvent.Tipo.EXPIRADA));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void procesar(ReservaNotificacionEvent event) {
        try {
            Reserva reserva = reservaRepository.findByUuid(event.getReservaUuid())
                    .orElseThrow(() -> new NotFoundException(RESERVA, event.getReservaUuid()));
            boolean expirada = event.getTipo() == ReservaNotificacionEvent.Tipo.EXPIRADA;
            notificar(
                    reserva,
                    expirada ? "Reserva expirada" : "Reserva cancelada",
                    "WARNING",
                    expirada,
                    expirada ? null : reserva.getAnuladoPor());
        } catch (RuntimeException ex) {
            logger.error(
                    "La reserva {} ya fue confirmada, pero fallo la preparacion de su notificacion {}",
                    event.getReservaUuid(),
                    event.getTipo(),
                    ex);
            notificacionOperativaService.error("Notificación de reserva " + event.getTipo(), ex.getMessage());
        }
    }

    private void notificar(Reserva reserva, String titulo, String prioridad, boolean expirada, String usuarioExcluidoUuid) {
        String contribuyenteUuid = null;
        if (reserva.getContribuyente() != null && reserva.getContribuyente().getUuid() != null) {
            contribuyenteUuid = reserva.getContribuyente().getUuid();
        }

        if (contribuyenteUuid == null) return;
        String resumen = expirada
                ? "La reserva expiró porque no se confirmó el pago dentro del plazo establecido"
                : "La reserva fue cancelada. Motivo: " + valor(reserva.getMotivoRechazo());
        NotificacionDto comun = base(
                reserva,
                ReservaNotificacionFormatter.titulo(titulo, reserva),
                ReservaNotificacionFormatter.mensaje(reserva, resumen),
                prioridad);
        String tipo = expirada ? NOTIFICAR_RESERVA_EXPIRADA : NOTIFICAR_RESERVA_ANULADA;
        comun.setTipo(tipo);
        destinatariosService.publicar(
                tipo,
                reserva,
                comun,
                comun,
                usuarioExcluidoUuid);
    }

    private NotificacionDto base(Reserva reserva, String titulo, String mensaje, String prioridad) {
        return new NotificacionDto()
                .setTipo(NOTIFICAR_RESERVA_ANULADA)
                .setTitulo(titulo)
                .setMensaje(mensaje)
                .setPrioridad(prioridad)
                .setCanal("APP")
                .setReferenciaTipo("RESERVA")
                .setReferenciaUuid(reserva.getUuid())
                .setAccionUrl(ReservaNotificacionFormatter.ruta(reserva));
    }

    private String mensajeContribuyente(Reserva reserva, boolean expirada) {
        if (expirada) {
            return "Tu reserva " + valor(reserva.getNumeroReserva()) + lugar(reserva)
                    + " expiró porque no se confirmó el pago dentro del plazo establecido.";
        }
        return "Tu reserva " + valor(reserva.getNumeroReserva()) + lugar(reserva)
                + " fue cancelada. Motivo: " + valor(reserva.getMotivoRechazo()) + ".";
    }

    private String mensajeInterno(Reserva reserva, boolean expirada) {
        String contribuyente = reserva.getContribuyente() == null
                ? "el contribuyente" : valor(reserva.getContribuyente().getNombreCompleto());
        if (expirada) {
            return "La reserva " + valor(reserva.getNumeroReserva()) + " de " + contribuyente + lugar(reserva)
                    + " expiró porque no se confirmó el pago dentro del plazo establecido.";
        }
        return "La reserva " + valor(reserva.getNumeroReserva()) + " de " + contribuyente + lugar(reserva)
                + " fue cancelada. Motivo: " + valor(reserva.getMotivoRechazo()) + ".";
    }

    private String lugar(Reserva reserva) {
        if (reserva.getCampoDeportivo() == null) return "";
        String lugar = " para " + valor(reserva.getCampoDeportivo().getNombre());
        if (reserva.getCampoDeportivo().getComplejo() != null) {
            lugar += " del complejo " + valor(reserva.getCampoDeportivo().getComplejo().getNombre());
        }
        return lugar;
    }

    private String ruta(Reserva reserva) {
        if ("recurrente".equalsIgnoreCase(reserva.getTipoReserva())) return "/reservations/recurrent";
        if ("periodo".equalsIgnoreCase(reserva.getTipoReserva())) return "/reservations/schools";
        return "/reservations/one-time";
    }

    private String valor(Object value) {
        return value == null ? "" : String.valueOf(value).replace("\"", "\\\"");
    }
}
