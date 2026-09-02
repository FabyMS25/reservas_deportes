package com.gamq.deportes.service.implement;

import com.gamq.deportes.model.Reserva;
import com.gamq.deportes.model.ReservaPuntual;
import com.gamq.deportes.model.ReservaRecurrente;

import java.time.format.DateTimeFormatter;

/** Builds the same concise reservation context for every notification recipient. */
public final class ReservaNotificacionFormatter {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private ReservaNotificacionFormatter() {
    }

    public static String titulo(String accion, Reserva reserva) {
        return accion + " · " + valor(reserva == null ? null : reserva.getNumeroReserva());
    }

    public static String mensaje(Reserva reserva, String resumen) {
        StringBuilder mensaje = new StringBuilder(resumen == null ? "" : resumen.trim());
        agregar(mensaje, "Solicitante", reserva != null && reserva.getContribuyente() != null
                ? reserva.getContribuyente().getNombreCompleto() : null);
        agregar(mensaje, "Lugar", lugar(reserva));
        agregar(mensaje, "Horario", horario(reserva));
        if (reserva != null && reserva.getMontoTotal() != null) {
            agregar(mensaje, "Monto", "Bs " + reserva.getMontoTotal());
        }
        return mensaje.toString();
    }

    public static String ruta(Reserva reserva) {
        if (reserva != null && "recurrente".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/recurrent";
        }
        if (reserva != null && "periodo".equalsIgnoreCase(reserva.getTipoReserva())) {
            return "/reservations/schools";
        }
        return "/reservations/one-time";
    }

    private static String lugar(Reserva reserva) {
        if (reserva == null || reserva.getCampoDeportivo() == null) return null;
        String lugar = valor(reserva.getCampoDeportivo().getNombre());
        if (reserva.getCampoDeportivo().getComplejo() != null) {
            lugar += " · " + valor(reserva.getCampoDeportivo().getComplejo().getNombre());
        }
        return lugar;
    }

    private static String horario(Reserva reserva) {
        if (reserva == null) return null;
        ReservaPuntual puntual = reserva.getReservaPuntual();
        if (puntual != null && puntual.getFechaUso() != null) {
            String horario = puntual.getFechaUso().format(FECHA);
            if (puntual.getHoraInicio() != null) {
                horario += " " + puntual.getHoraInicio().format(HORA);
                if (puntual.getHoraFin() != null) horario += "–" + puntual.getHoraFin().format(HORA);
            }
            return horario;
        }
        ReservaRecurrente recurrente = reserva.getReservaRecurrente();
        if (recurrente != null && recurrente.getFechaInicio() != null) {
            String horario = recurrente.getFechaInicio().format(FECHA);
            if (recurrente.getFechaFin() != null) horario += "–" + recurrente.getFechaFin().format(FECHA);
            if (recurrente.getDiasSemana() != null && !recurrente.getDiasSemana().isBlank()) {
                horario += " · " + recurrente.getDiasSemana();
            }
            return horario;
        }
        return null;
    }

    private static void agregar(StringBuilder mensaje, String etiqueta, String contenido) {
        if (contenido == null || contenido.isBlank()) return;
        if (mensaje.length() > 0 && mensaje.charAt(mensaje.length() - 1) != '.') mensaje.append('.');
        if (mensaje.length() > 0) mensaje.append(' ');
        mensaje.append(etiqueta).append(": ").append(contenido).append('.');
    }

    private static String valor(Object value) {
        return value == null ? "Sin dato" : String.valueOf(value).replace("\"", "\\\"");
    }
}
