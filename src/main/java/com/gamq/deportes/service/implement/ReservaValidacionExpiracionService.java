package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaAuditoria;
import com.gamq.deportes.model.ReservaPuntual;
import com.gamq.deportes.model.ReservaRecurrente;
import com.gamq.deportes.repository.ReservaAuditoriaRepository;
import com.gamq.deportes.repository.ReservaPuntualRepository;
import com.gamq.deportes.repository.ReservaRecurrenteRepository;
import com.gamq.deportes.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class ReservaValidacionExpiracionService {

    private static final String MOTIVO = "RESERVA VENCIDA SIN VALIDACIÓN ADMINISTRATIVA";
    private final ReservaPuntualRepository puntualRepository;
    private final ReservaRecurrenteRepository recurrenteRepository;
    private final ReservaRepository reservaRepository;
    private final ReservaAuditoriaRepository auditoriaRepository;
    private final ReservaNotificacionService notificacionService;

    public ReservaValidacionExpiracionService(ReservaPuntualRepository puntualRepository,
                                              ReservaRecurrenteRepository recurrenteRepository,
                                              ReservaRepository reservaRepository,
                                              ReservaAuditoriaRepository auditoriaRepository,
                                              ReservaNotificacionService notificacionService) {
        this.puntualRepository = puntualRepository;
        this.recurrenteRepository = recurrenteRepository;
        this.reservaRepository = reservaRepository;
        this.auditoriaRepository = auditoriaRepository;
        this.notificacionService = notificacionService;
    }

    @Transactional
    public void procesar() {
        // Disabled until an explicit request-expiration policy is configured.
        // Usage dates must not substitute a SOLICITADA expiration deadline.
        /*
        LocalDate fecha = LocalDate.now();
        Set<String> procesadas = new LinkedHashSet<>();
        for (ReservaPuntual puntual : puntualRepository.findSolicitudesVencidas(fecha, LocalTime.now())) {
            vencer(puntual.getReserva(), procesadas);
        }
        for (ReservaRecurrente recurrente : recurrenteRepository.findSolicitudesVencidas(fecha)) {
            vencer(recurrente.getReserva(), procesadas);
        }
        */
    }

    private void vencer(Reserva reserva, Set<String> procesadas) {
        if (reserva == null || reserva.getUuid() == null || !procesadas.add(reserva.getUuid())) return;
        if (!"SOLICITADA".equalsIgnoreCase(reserva.getEstadoReserva())
                || !"PENDIENTE".equalsIgnoreCase(reserva.getEstadoValidacion())) return;
        // An issued tax cannot be cancelled by this local-only administrative path.
        // It requires the explicit RUAT-confirmed annulment flow.
        if (reserva.getNumeroTasa() != null && !reserva.getNumeroTasa().isBlank()) return;

        String anterior = reserva.getEstadoReserva();
        reserva.setEstadoReserva("CANCELADA");
        reserva.setEstadoValidacion("VENCIDA");
        reserva.setMotivoRechazo(MOTIVO);
        reserva.setFechaRechazo(LocalDateTime.now());
        reserva.setAnuladoPor(null);
        reserva.setFechaActualizacion(LocalDateTime.now());
        Reserva guardada = reservaRepository.save(reserva);

        auditoriaRepository.save(new ReservaAuditoria()
                .setReserva(guardada)
                .setAccion("EXPIRE_VALIDATION")
                .setEstadoAnterior(anterior)
                .setEstadoNuevo("CANCELADA")
                .setUsuarioUuid(null)
                .setObservaciones(MOTIVO));

        notificacionService.notificarCancelada(guardada);
    }
}
