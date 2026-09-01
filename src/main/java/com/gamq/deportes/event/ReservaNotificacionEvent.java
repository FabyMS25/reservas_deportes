package com.gamq.deportes.event;

public class ReservaNotificacionEvent {

    public enum Tipo {
        CANCELADA,
        EXPIRADA
    }

    private final String reservaUuid;
    private final Tipo tipo;

    public ReservaNotificacionEvent(String reservaUuid, Tipo tipo) {
        this.reservaUuid = reservaUuid;
        this.tipo = tipo;
    }

    public String getReservaUuid() {
        return reservaUuid;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
