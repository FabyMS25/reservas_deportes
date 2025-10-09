package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaAuditoriaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaAuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-auditorias")
@Tag(name = "Reservas Auditorías", description = "API para gestión de auditorías de reservas")
public class ReservaAuditoriaController {

    @Autowired
    ReservaAuditoriaService reservaAuditoriaService;

    @Operation(summary = "Obtener todas las auditorías")
    @GetMapping
    public Response obtenerTodasAuditorias(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaAuditoriaDto> auditoriasPage = reservaAuditoriaService.obtenerTodasAuditoriasPage(pageable);
                return Response.ok().setPayload(auditoriasPage);
            } else {
                List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.obtenerTodasAuditorias();
                return Response.ok().setPayload(auditoriasDtos);
            }
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener auditorías: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditoría por UUID")
    @GetMapping("/{uuid}")
    public Response buscarAuditoriaPorUuid(
            @Parameter(description = "UUID de la auditoría")
            @PathVariable("uuid") String uuid) {
        try {
            ReservaAuditoriaDto auditoriaDto = reservaAuditoriaService.findByUuid(uuid);
            return Response.ok().setPayload(auditoriaDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Auditoría no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías por reserva")
    @GetMapping("/reserva/{reservaUuid}")
    public Response buscarAuditoriasPorReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("reservaUuid") String reservaUuid,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaAuditoriaDto> auditoriasPage = reservaAuditoriaService.findByReservaUuid(reservaUuid, pageable);
                return Response.ok().setPayload(auditoriasPage);
            } else {
                List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.findByReservaUuid(reservaUuid);
                return Response.ok().setPayload(auditoriasDtos);
            }
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías por acción")
    @GetMapping("/accion/{accion}")
    public Response buscarAuditoriasPorAccion(
            @Parameter(description = "Tipo de acción")
            @PathVariable("accion") String accion) {
        try {
            List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.findByAccion(accion);
            return Response.ok().setPayload(auditoriasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por acción: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías por usuario")
    @GetMapping("/usuario/{usuarioUuid}")
    public Response buscarAuditoriasPorUsuario(
            @Parameter(description = "UUID del usuario")
            @PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.findByUsuarioUuid(usuarioUuid);
            return Response.ok().setPayload(auditoriasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por usuario: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías por rango de fechas")
    @GetMapping("/fechas")
    public Response buscarAuditoriasPorFecha(
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaAuditoriaDto> auditoriasPage = reservaAuditoriaService.findByFechaAccionBetween(fechaInicio, fechaFin, pageable);
                return Response.ok().setPayload(auditoriasPage);
            } else {
                List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.findByFechaAccionBetween(fechaInicio, fechaFin);
                return Response.ok().setPayload(auditoriasDtos);
            }
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener últimas auditorías de una reserva")
    @GetMapping("/reserva/{reservaUuid}/ultimas")
    public Response obtenerUltimasAuditoriasPorReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("reservaUuid") String reservaUuid,
            @Parameter(description = "Límite de registros") @RequestParam(defaultValue = "10") int limite) {
        try {
            List<ReservaAuditoriaDto> auditoriasDtos = reservaAuditoriaService.findLatestByReservaUuid(reservaUuid, limite);
            return Response.ok().setPayload(auditoriasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al obtener últimas auditorías: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Contar auditorías por reserva")
//    @GetMapping("/contador/reserva/{reservaUuid}")
//    public Response contarAuditoriasPorReserva(
//            @Parameter(description = "UUID de la reserva")
//            @PathVariable("reservaUuid") String reservaUuid) {
//        try {
//            Long cantidad = reservaAuditoriaService.countByReservaUuid(reservaUuid);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.exception().setPayload("Error al contar auditorías: " + ex.getMessage());
//        }
//    }

//    @Operation(summary = "Contar auditorías por acción")
//    @GetMapping("/contador/accion/{accion}")
//    public Response contarAuditoriasPorAccion(
//            @Parameter(description = "Tipo de acción")
//            @PathVariable("accion") String accion) {
//        try {
//            Long cantidad = reservaAuditoriaService.countByAccion(accion);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.exception().setPayload("Error al contar auditorías: " + ex.getMessage());
//        }
//    }

    @Operation(summary = "Obtener estadísticas por acción")
    @GetMapping("/estadisticas/accion")
    public Response obtenerEstadisticasPorAccion() {
        try {
            List<Object[]> estadisticas = reservaAuditoriaService.getEstadisticasPorAccion();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por usuario")
    @GetMapping("/estadisticas/usuario")
    public Response obtenerEstadisticasPorUsuario() {
        try {
            List<Object[]> estadisticas = reservaAuditoriaService.getEstadisticasPorUsuario();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por fecha")
    @GetMapping("/estadisticas/fecha")
    public Response obtenerEstadisticasPorFecha(
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaInicio,
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin) {
        try {
            List<Object[]> estadisticas = reservaAuditoriaService.getEstadisticasPorFecha(fechaInicio, fechaFin);
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Registrar acción de auditoría")
    @PostMapping("/registrar")
    public Response registrarAccionAuditoria(
            @Parameter(description = "UUID de la reserva") @RequestParam String reservaUuid,
            @Parameter(description = "Tipo de acción") @RequestParam String accion,
            @Parameter(description = "UUID del usuario") @RequestParam String usuarioUuid,
            @Parameter(description = "Estado anterior") @RequestParam(required = false) String estadoAnterior,
            @Parameter(description = "Estado nuevo") @RequestParam(required = false) String estadoNuevo,
            @Parameter(description = "Datos de cambio (JSON)") @RequestParam(required = false) String datosCambio,
            @Parameter(description = "Observaciones") @RequestParam(required = false) String observaciones,
            @Parameter(description = "Dirección IP") @RequestParam(required = false) String direccionIp) {
        try {
            reservaAuditoriaService.registrarAccion(reservaUuid, accion, usuarioUuid, estadoAnterior, estadoNuevo, datosCambio, observaciones, direccionIp);
            return Response.ok().setPayload("Acción de auditoría registrada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al registrar acción de auditoría: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar auditoría")
    @DeleteMapping("/{uuid}")
    public Response eliminarAuditoria(
            @Parameter(description = "UUID de la auditoría")
            @PathVariable("uuid") String uuid) {
        try {
            reservaAuditoriaService.eliminarAuditoria(uuid);
            return Response.ok().setPayload("Auditoría eliminada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar auditoría: " + ex.getMessage());
        }
    }
}