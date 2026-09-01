package com.gamq.deportes.event;

import com.gamq.deportes.dto.NotificacionDto;

public class NotificacionEvent {

    public enum Operacion {
        CREAR,
        CREAR_ADMIN_SI_HABILITADA
    }

    private final Operacion operacion;
    private final String claveConfiguracion;
    private final NotificacionDto notificacion;
    private final String usuarioExcluidoUuid;

    private NotificacionEvent(Operacion operacion, String claveConfiguracion,
                              NotificacionDto notificacion, String usuarioExcluidoUuid) {
        this.operacion = operacion;
        this.claveConfiguracion = claveConfiguracion;
        this.notificacion = notificacion;
        this.usuarioExcluidoUuid = usuarioExcluidoUuid;
    }

    public static NotificacionEvent crear(NotificacionDto notificacion) {
        return new NotificacionEvent(Operacion.CREAR, null, notificacion, null);
    }

    public static NotificacionEvent crearAdminSiHabilitada(String claveConfiguracion,
                                                            NotificacionDto notificacion,
                                                            String usuarioExcluidoUuid) {
        return new NotificacionEvent(
                Operacion.CREAR_ADMIN_SI_HABILITADA,
                claveConfiguracion,
                notificacion,
                usuarioExcluidoUuid);
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public String getClaveConfiguracion() {
        return claveConfiguracion;
    }

    public NotificacionDto getNotificacion() {
        return notificacion;
    }

    public String getUsuarioExcluidoUuid() {
        return usuarioExcluidoUuid;
    }
}
