package com.gamq.deportes.controller;


import com.gamq.deportes.dto.ReservaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaService;
import com.gamq.deportes.service.implement.ReservationNumberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    @Autowired
    ReservaService reservaService;

    @Autowired
    ReservationNumberService reservationNumberService;

//    @GetMapping()
//    public Response obtenerTodasLasReservas() {
//        try{
//            List<ReservaDto> reservasDtos =  reservaService.obtenerReservas();
//            return Response.ok().setPayload(reservasDtos);
//        } catch(Exception ex) {
//            return Response.notContent().setPayload(ex.getMessage());
//        }
//    }
    @Operation(summary = "Obtener todas las reservas")
    @GetMapping()
    public Response obtenerReservas(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaDto> reservasPage = reservaService.obtenerReservas(pageable);
                return Response.ok().setPayload(reservasPage);
            } else {
                List<ReservaDto> reservasDtos = reservaService.obtenerReservas();
                return Response.ok().setPayload(reservasDtos);
            }
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reserva por UUID")
    @GetMapping("/{uuid}")
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
    public Response crearReserva(@RequestBody ReservaDto reservaDto) {
        try {
            ReservaDto nuevaReserva = reservaService.crearReserva(reservaDto);
            return Response.ok().setPayload(nuevaReserva);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Actualizar reserva existente")
    @PutMapping()
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

    @Operation(summary = "Rechazar reserva")
    @PutMapping("/{uuid}/rechazar")
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
    public Response cancelarReserva(
            @Parameter(description = "UUID de la reserva")
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Motivo de la cancelación")
            @RequestParam String motivo) {
        try {
            ReservaDto reservaCancelada = reservaService.cancelarReserva(uuid, motivo);
            return Response.ok().setPayload(reservaCancelada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al cancelar reserva: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar reserva")
    @DeleteMapping("/{uuid}")
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
}
