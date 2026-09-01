package com.gamq.deportes.controller;

import com.gamq.deportes.dto.NotificacionDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(name = "Notificaciones", description = "API para notificaciones de usuarios")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    @Operation(summary = "Buscar notificacion por UUID")
    @GetMapping("/{uuid}")
    public Response buscarPorUuid(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(notificacionService.findByUuid(uuid));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Notificacion no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener notificaciones por usuario")
    @GetMapping("/usuario/{usuarioUuid}")
    public Response obtenerPorUsuario(
            @PathVariable("usuarioUuid") String usuarioUuid,
            @Parameter(description = "Numero de pagina") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamano de pagina") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<NotificacionDto> notificacionesPage = notificacionService.obtenerPorUsuario(usuarioUuid, pageable);
                return Response.ok().setPayload(notificacionesPage);
            }
            List<NotificacionDto> notificaciones = notificacionService.obtenerPorUsuario(usuarioUuid);
            return Response.ok().setPayload(notificaciones);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al obtener notificaciones: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener notificaciones no leidas por usuario")
    @GetMapping("/usuario/{usuarioUuid}/no-leidas")
    public Response obtenerNoLeidas(@PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            return Response.ok().setPayload(notificacionService.obtenerNoLeidas(usuarioUuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al obtener notificaciones no leidas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Contar notificaciones no leidas por usuario")
    @GetMapping("/usuario/{usuarioUuid}/contador-no-leidas")
    public Response contarNoLeidas(@PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            return Response.ok().setPayload(notificacionService.contarNoLeidas(usuarioUuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al contar notificaciones no leidas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Sincronizar notificaciones administrativas recientes")
    @PostMapping("/admin/sincronizar/{usuarioUuid}")
    public Response sincronizarNotificacionesAdmin(@PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            return Response.ok().setPayload(notificacionService.sincronizarNotificacionesAdmin(usuarioUuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al sincronizar notificaciones administrativas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Marcar notificacion como leida")
    @PatchMapping("/{uuid}/leer")
    public Response marcarComoLeida(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(notificacionService.marcarComoLeida(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al marcar notificacion como leida: " + ex.getMessage());
        }
    }

    @Operation(summary = "Marcar todas las notificaciones del usuario como leidas")
    @PatchMapping("/usuario/{usuarioUuid}/leer-todas")
    public Response marcarTodasComoLeidas(@PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            return Response.ok().setPayload(notificacionService.marcarTodasComoLeidas(usuarioUuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al marcar notificaciones como leidas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar notificacion")
    @DeleteMapping("/{uuid}")
    public Response eliminarNotificacion(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(notificacionService.eliminarNotificacion(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar notificacion: " + ex.getMessage());
        }
    }
}
