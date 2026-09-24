package com.gamq.deportes.controller;


import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaService;
import com.gamq.deportes.service.implement.ReservationNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "API para Reservas en Global")
public class ReservaController {

    @Autowired
    ReservaService reservaService;

    @Autowired
    ReservationNumberService reservationNumberService;

    @Operation(summary = "Obtener todas las reservas")
    @GetMapping
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response obtenerReservas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean revisarEstados,
            @RequestParam(required = false) Integer tab,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String typeReserva,
            @RequestParam(required = false) String estadoReserva,
            @RequestParam(required = false) String estadoValidacion,
            @RequestParam(required = false) Boolean esReservaGratuita,
            @RequestParam(required = false) String organization,
            @RequestParam(required = false) Integer mesFiltro,
            @RequestParam(required = false) Integer anioFiltro,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                com.gamq.deportes.service.implement.ReservaServiceImpl.ordenListado(sortBy, sortDirection, "periodo")
        );
        return Response.ok().setPayload(
                reservaService.obtenerReservas(
                        pageable,
                        tab,
                        searchTerm,
                        typeReserva,
                        estadoReserva,
                        estadoValidacion,
                        esReservaGratuita,
                        organization,
                        mesFiltro,
                        anioFiltro,
                        revisarEstados
                )
        );
    }

    @Operation(summary = "Buscar reserva por UUID")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.reservation('VER',#p0)")
    public Response buscarReservaPorUuid(@Parameter(description = "UUID de la reserva")
                                         @PathVariable("uuid") String uuid) {
        try {
            ReservaDto reservaDto = reservaService.findByUuid(uuid);
            return Response.ok().setPayload(reservaDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reserva por número de reserva")
    @GetMapping("/numero/{numeroReserva}")
    @PreAuthorize("@permissions.reservationNumber('VER',#p0)")
    public Response buscarReservaPorNumero(@Parameter(description = "Número de reserva")
                                           @PathVariable("numeroReserva") String numeroReserva) {
        try {
            ReservaDto reservaDto = reservaService.findByNumeroReserva(numeroReserva);
            return Response.ok().setPayload(reservaDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por rango de fechas")
    @GetMapping("/fechas")
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response buscarPorFechaReserva(
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd HH:mm)")
            @RequestParam(name = "fechaIni") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaIni,
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd HH:mm)")
            @RequestParam(name = "fechaFin") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaDto> reservasPage = reservaService.buscarPorFechaReserva(fechaIni, fechaFin, pageable);
                return Response.ok().setPayload(reservasPage);
            } else {
                List<ReservaDto> reservasDtos = reservaService.buscarPorFechaReserva(fechaIni, fechaFin);
                return Response.ok().setPayload(reservasDtos);
            }
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por campo deportivo")
    @GetMapping("/campo/{uuidCampo}")
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response buscarReservasPorCampoDeportivo(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("uuidCampo") String uuidCampo) {
        try {
            List<ReservaDto> reservasDtos = reservaService.findReservasByCampoDeportivo(uuidCampo);
            return Response.ok().setPayload(reservasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por campo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por contribuyente")
    @GetMapping("/contribuyente/{uuidContribuyente}")
    @PreAuthorize("@permissions.anyReservation('VER') and (@permissions.staff() or @permissions.self(#p0))")
    public Response buscarReservasPorContribuyente(
            @Parameter(description = "UUID del contribuyente")
            @PathVariable("uuidContribuyente") String uuidContribuyente) {
        try {
            List<ReservaDto> reservasDtos = reservaService.findReservasByContribuyente(uuidContribuyente);
            return Response.ok().setPayload(reservasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por contribuyente: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por estado")
    @GetMapping("/estado/{estadoReserva}")
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response buscarReservasPorEstado(
            @Parameter(description = "Estado de la reserva")
            @PathVariable("estadoReserva") String estadoReserva) {
        try {
            List<ReservaDto> reservasDtos = reservaService.findByEstadoReserva(estadoReserva);
            return Response.ok().setPayload(reservasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por estado: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por tipo")
    @GetMapping("/tipo/{tipoReserva}")
    @PreAuthorize("@permissions.reservationType('VER',#p0)")
    public Response buscarReservasPorTipo(
            @Parameter(description = "Tipo de reserva")
            @PathVariable("tipoReserva") String tipoReserva) {
        try {
            List<ReservaDto> reservasDtos = reservaService.findByTipoReserva(tipoReserva);
            return Response.ok().setPayload(reservasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por tipo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas por campo y fecha")
    @GetMapping("/activas/campo/{uuidCampo}")
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response buscarReservasActivasPorCampoYFecha(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("uuidCampo") String uuidCampo,
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd HH:mm)")
            @RequestParam(name = "fechaIni") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaIni,
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd HH:mm)")
            @RequestParam(name = "fechaFin") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin) {
        try {
            List<ReservaDto> reservasDtos = reservaService.findReservasActivasPorCampoYFecha(uuidCampo, fechaIni, fechaFin);
            return Response.ok().setPayload(reservasDtos);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas activas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Previsualizar próximo número de reserva")
    @GetMapping("/numero-reserva/preview")
    @PreAuthorize("@permissions.anyReservation('CREAR')")
    public Response previsualizarNumeroReserva(
            @Parameter(description = "Tipo de reserva: puntual, recurrente, periodo", required = true)
            @RequestParam String tipo) {
        try {
            String numeroReserva = reservationNumberService.previewNextReservationNumber(tipo);
            return Response.ok().setPayload(numeroReserva);
        } catch (IllegalArgumentException ex) {
            return Response.badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al previsualizar número de reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Generar próximo número de reserva disponible")
    @GetMapping("/numero-reserva/generar")
    @PreAuthorize("@permissions.anyReservation('CREAR')")
    public Response generarNumeroReserva(
            @Parameter(description = "Tipo de reserva: puntual, recurrente, periodo", required = true)
            @RequestParam String tipo) {
        try {
            String numeroReserva = reservationNumberService.findNextAvailableReservationNumber(tipo);
            return Response.ok().setPayload(numeroReserva);
        } catch (IllegalArgumentException ex) {
            return Response.badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al generar número de reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Validar número de reserva personalizado")
    @PostMapping("/numero-reserva/validar")
    @PreAuthorize("@permissions.anyReservation('CREAR')")
    public Response validarNumeroReservaPersonalizado(
            @Parameter(description = "Número de reserva a validar")
            @RequestBody Map<String, String> request) {
        try {
            String numeroReserva = request.get("numeroReserva");
            String tipoReserva = request.get("tipoReserva");

            if (tipoReserva == null || tipoReserva.trim().isEmpty()) {
                return Response.badRequest().setPayload("El tipo de reserva es requerido");
            }

            // Check format
            if (!reservationNumberService.isReservationNumberValid(numeroReserva, tipoReserva)) {
                return Response.badRequest().setPayload("Formato inválido. Debe ser: P-0010/25, R-0010/25, o E-0010/25 según el tipo de reserva");
            }

            // Check if available
            if (!reservationNumberService.isReservationNumberAvailable(numeroReserva)) {
                return Response.unauthorized().setPayload("El número de reserva ya está en uso");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("message", "Número de reserva válido y disponible");
            response.put("numeroReserva", numeroReserva);

            return Response.ok().setPayload(response);
        } catch (IllegalArgumentException ex) {
            return Response.badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al validar número de reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Verificar disponibilidad de número de reserva")
    @GetMapping("/numero-reserva/verificar/{numeroReserva}")
    @PreAuthorize("@permissions.anyReservation('CREAR')")
    public Response verificarDisponibilidadNumeroReserva(
            @Parameter(description = "Número de reserva a verificar")
            @PathVariable String numeroReserva) {
        try {
            boolean disponible = reservationNumberService.isReservationNumberAvailable(numeroReserva);

            Map<String, Object> response = new HashMap<>();
            response.put("disponible", disponible);
            response.put("numeroReserva", numeroReserva);

            if (!disponible) {
                response.put("mensaje", "El número de reserva no está disponible");
            } else {
                response.put("mensaje", "El número de reserva está disponible");
            }

            return Response.ok().setPayload(response);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al verificar disponibilidad: " + ex.getMessage());
        }
    }

    @Operation(summary = "Verificar conflictos de reserva")
    @GetMapping("/conflictos")
    @PreAuthorize("@permissions.anyReservation('VER')")
    public Response verificarConflictosReserva(
            @Parameter(description = "UUID del campo deportivo")
            @RequestParam String uuidCampo,
            @Parameter(description = "Fecha inicial (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaIni,
            @Parameter(description = "Fecha final (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaFin,
            @Parameter(description = "UUID de reserva a excluir (opcional)")
            @RequestParam(required = false) String excludeReservaUuid) {
        try {
            boolean existeConflicto = reservaService.existeReservaConflictiva(uuidCampo, fechaIni, fechaFin, excludeReservaUuid);
            return Response.ok().setPayload(existeConflicto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al verificar conflictos: " + ex.getMessage());
        }
    }

    @Operation(summary = "Crear nueva reserva")
    @PostMapping()
    @PreAuthorize("@permissions.reservationBody('CREAR',#p0)")
    public Response crearReserva(@RequestBody ReservaDto reservaDto) {
        try {
            ReservaDto nuevaReserva = reservaService.crearReserva(reservaDto);
            return Response.ok().setPayload(nuevaReserva);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear reserva: " + getRootMessage(ex));
        }
    }

    @Operation(summary = "Actualizar reserva existente")
    @PutMapping()
    @PreAuthorize("@permissions.reservationBody('ACTUALIZAR',#p0)")
    public Response actualizarReserva(@RequestBody ReservaDto reservaDto) {
        try {
            ReservaDto reservaActualizada = reservaService.actualizarReserva(reservaDto);
            return Response.ok().setPayload(reservaActualizada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al actualizar reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Aprobar reserva")
    @PutMapping("/{uuid}/aprobar")
    @PreAuthorize("@permissions.reservation('VALIDAR',#p0)")
    public Response aprobarReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Usuario que aprueba")
            @RequestParam String aprobadoPor) {
        try {
            ReservaDto reservaAprobada = reservaService.aprobarReserva(uuid, aprobadoPor);
            return Response.ok().setPayload(reservaAprobada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al aprobar reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Completar reserva recurrente gratuita de escuela")
    @PutMapping("/completar-escuela")
    @PreAuthorize("@permissions.reservation('ACTUALIZAR',#p0)")
    public Response completarReservaEscuela(@RequestParam("uuid") String uuid) {
        try {
            return Response.ok().setPayload(reservaService.completarReservaEscuela(uuid));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Registrar No Show")
    @PutMapping("/update-not-show")
    @PreAuthorize("@permissions.reservation('ACTUALIZAR',#p0)")
    public Response marcarNoShow(@RequestParam("uuid") String uuid,
                                @RequestParam(required = false) String observacion) {
        return actualizarEstadoUso(uuid, "NO_SHOW", observacion);
    }

    @Operation(summary = "Confirmar uso completo de reserva")
    @PutMapping("/completar")
    @PreAuthorize("@permissions.reservation('ACTUALIZAR',#p0)")
    public Response completarUso(@RequestParam("uuid") String uuid) {
        return actualizarEstadoUso(uuid, "COMPLETADA", null);
    }

    private Response actualizarEstadoUso(String uuid, String estado, String observacion) {
        try {
            return Response.ok().setPayload(reservaService.actualizarEstadoUso(uuid, estado, observacion));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Rechazar reserva")
    @PutMapping("/{uuid}/rechazar")
    @PreAuthorize("@permissions.reservation('VALIDAR',#p0)")
    public Response rechazarReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Motivo del rechazo")
            @RequestParam String motivoRechazo,
            @Parameter(description = "Usuario que rechaza")
            @RequestParam String aprobadoPor) {
        try {
            ReservaDto reservaRechazada = reservaService.rechazarReserva(uuid, motivoRechazo, aprobadoPor);
            return Response.ok().setPayload(reservaRechazada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al rechazar reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Cancelar reserva")
    @PutMapping("/{uuid}/cancelar")
    @PreAuthorize("@permissions.reservation('ANULAR',#p0)")
    public Response cancelarReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Motivo de la cancelación")
            @RequestParam String motivo,
            @RequestParam(required = false) String observacion,
            @Parameter(description = "UUID del usuario que realiza la cancelación")
            @RequestParam(required = false) String anuladoPor) {
        try {
            var reservaCancelada = reservaService.cancelarReserva(uuid, motivo, observacion, anuladoPor);
            return Response.ok().setPayload(reservaCancelada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al cancelar reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar reserva")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.reservation('ANULAR',#p0)")
    public Response eliminarReserva(@Parameter(description = "UUID de la reserva")
                                    @PathVariable("uuid") String uuid) {
        try {
            reservaService.eliminarReserva(uuid);
            return Response.ok().setPayload("Reserva eliminada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar reserva: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Contar reservas por estado")
//    @GetMapping("/contador/estado/{estadoReserva}")
//    public Response contarReservasPorEstado(@Parameter(description = "Estado de la reserva")
//                                            @PathVariable("estadoReserva") String estadoReserva) {
//        try {
//            Long cantidad = reservaService.contarPorEstadoReserva(estadoReserva);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar reservas: " + ex.getMessage());
//        }
//    }
//
//    @Operation(summary = "Contar reservas por campo y estado")
//    @GetMapping("/contador/campo/{uuidCampo}/estado/{estadoReserva}")
//    public Response contarReservasPorCampoYEstado(
//            @Parameter(description = "UUID del campo deportivo")
//            @PathVariable("uuidCampo") String uuidCampo,
//            @Parameter(description = "Estado de la reserva")
//            @PathVariable("estadoReserva") String estadoReserva) {
//        try {
//            Long cantidad = reservaService.contarPorCampoDeportivoYEstado(uuidCampo, estadoReserva);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar reservas: " + ex.getMessage());
//        }
//    }

    private String getRootMessage(Exception ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage();
    }
}
