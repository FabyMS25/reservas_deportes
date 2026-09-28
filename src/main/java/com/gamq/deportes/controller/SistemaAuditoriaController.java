package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.SistemaAuditoriaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.SistemaAuditoriaService;
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

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/sistema-auditorias")
@Tag(name = "Sistema Auditorías", description = "API para gestión de auditorías generales del sistema")
public class SistemaAuditoriaController {

    @Autowired
    SistemaAuditoriaService sistemaAuditoriaService;

    @Operation(summary = "Obtener auditorías de sistema")
    @GetMapping
    @PreAuthorize("@permissions.admin()")
    public Response obtenerSistemaAuditorias(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SistemaAuditoriaDto> auditoriasPage = sistemaAuditoriaService.obtenerSistemaAuditorias(pageable);
            return Response.ok().setPayload(auditoriasPage);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener auditorías de sistema: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditoría de sistema por UUID")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.admin()")
    public Response buscarSistemaAuditoriaPorUuid(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.findByUuid(uuid));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Auditoría de sistema no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías de sistema por usuario")
    @GetMapping("/usuario/{usuarioUuid}")
    @PreAuthorize("@permissions.admin()")
    public Response buscarPorUsuario(@PathVariable("usuarioUuid") String usuarioUuid) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.findByUsuarioUuid(usuarioUuid));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por usuario: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías de sistema por acción")
    @GetMapping("/accion/{accion}")
    @PreAuthorize("@permissions.admin()")
    public Response buscarPorAccion(@PathVariable("accion") String accion) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.findByAccion(accion));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por acción: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías de sistema por módulo")
    @GetMapping("/modulo/{modulo}")
    @PreAuthorize("@permissions.admin()")
    public Response buscarPorModulo(@PathVariable("modulo") String modulo) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.findByModulo(modulo));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por módulo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías de sistema por entidad")
    @GetMapping("/entidad/{entidadTipo}/{entidadUuid}")
    @PreAuthorize("@permissions.admin()")
    public Response buscarPorEntidad(@PathVariable("entidadTipo") String entidadTipo,
                                     @PathVariable("entidadUuid") String entidadUuid) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.findByEntidad(entidadTipo, entidadUuid));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por entidad: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar auditorías de sistema por rango de fechas")
    @GetMapping("/fechas")
    @PreAuthorize("@permissions.admin()")
    public Response buscarPorFechas(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaInicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return Response.ok().setPayload(sistemaAuditoriaService.findByFechaAccionBetween(fechaInicio, fechaFin, pageable));
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar auditorías por fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Crear auditoría de sistema")
    @PostMapping
    @PreAuthorize("denyAll()") // Audit events are generated by business services only.
    public Response crearSistemaAuditoria(@RequestBody SistemaAuditoriaDto auditoriaDto) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.crearSistemaAuditoria(auditoriaDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear auditoría de sistema: " + ex.getMessage());
        }
    }

    @Operation(summary = "Registrar acción de sistema")
    @PostMapping("/registrar")
    @PreAuthorize("denyAll()")
    public Response registrarAccion(
            @RequestParam(required = false) String usuarioUuid,
            @RequestParam String accion,
            @RequestParam String modulo,
            @RequestParam(required = false) String entidadTipo,
            @RequestParam(required = false) String entidadUuid,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String datosCambio,
            @RequestParam(required = false) String direccionIp,
            @RequestParam(required = false) String userAgent) {
        try {
            sistemaAuditoriaService.registrarAccion(usuarioUuid, accion, modulo, entidadTipo, entidadUuid,
                    descripcion, datosCambio, direccionIp, userAgent);
            return Response.ok().setPayload("Acción de sistema registrada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al registrar acción de sistema: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por acción")
    @GetMapping("/estadisticas/accion")
    @PreAuthorize("@permissions.admin()")
    public Response obtenerEstadisticasPorAccion() {
        try {
            List<Object[]> estadisticas = sistemaAuditoriaService.getEstadisticasPorAccion();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por módulo")
    @GetMapping("/estadisticas/modulo")
    @PreAuthorize("@permissions.admin()")
    public Response obtenerEstadisticasPorModulo() {
        try {
            List<Object[]> estadisticas = sistemaAuditoriaService.getEstadisticasPorModulo();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener estadísticas por usuario")
    @GetMapping("/estadisticas/usuario")
    @PreAuthorize("@permissions.admin()")
    public Response obtenerEstadisticasPorUsuario() {
        try {
            List<Object[]> estadisticas = sistemaAuditoriaService.getEstadisticasPorUsuario();
            return Response.ok().setPayload(estadisticas);
        } catch (Exception ex) {
            return Response.exception().setPayload("Error al obtener estadísticas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar auditoría de sistema")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("denyAll()")
    public Response eliminarSistemaAuditoria(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(sistemaAuditoriaService.delete(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar auditoría de sistema: " + ex.getMessage());
        }
    }
}
