package com.gamq.deportes.service;

import com.gamq.deportes.dto.NotificacionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface NotificacionService {
    NotificacionDto findByUuid(String uuid);
    List<NotificacionDto> obtenerPorUsuario(String usuarioUuid);
    Page<NotificacionDto> obtenerPorUsuario(String usuarioUuid, Pageable pageable);
    List<NotificacionDto> obtenerNoLeidas(String usuarioUuid);
    Long contarNoLeidas(String usuarioUuid);
    NotificacionDto crearNotificacion(NotificacionDto notificacionDto);
    void crearNotificacionAdminSiHabilitada(String claveConfiguracion, NotificacionDto notificacionDto, String usuarioExcluidoUuid);
    List<NotificacionDto> sincronizarNotificacionesAdmin(String usuarioUuid);
    int limpiarNotificacionesAdminAntiguas(int diasRetencion);
    NotificacionDto marcarComoLeida(String uuid);
    List<NotificacionDto> marcarTodasComoLeidas(String usuarioUuid);
    String eliminarNotificacion(String uuid);
    boolean estaHabilitada(String claveConfiguracion);
}
