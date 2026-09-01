package com.gamq.deportes.event;

public class PagoConfirmadoEvent {

    private final String pagoUuid;

    public PagoConfirmadoEvent(String pagoUuid) {
        this.pagoUuid = pagoUuid;
    }

    public String getPagoUuid() {
        return pagoUuid;
    }
}
