package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaRecurrenteDto;
import com.gamq.deportes.dto.ReservaRecurrenteFilter;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaRecurrenteService;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-recurrentes")
@Tag(name = "Reservas Recurrentes", description = "API para gestión de reservas recurrentes")
public class ReservaRecurrenteController {
    @Autowired
    private ReservaRecurrenteService reservaRecurrenteService;

    @Operation(summary = "Obtener todas las reservas con filtros y paginación")
    @GetMapping()
    public Response obtenerReservas(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Pestaña seleccionada (0=Todas,1=Solicitadas,2=Pendientes,etc)")
            @RequestParam(required = false) Integer tab,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Estado de reserva")   @RequestParam(required = false) String estadoReserva,
            @Parameter(description = "Estado de validación")@RequestParam(required = false) String estadoValidacion,
            @Parameter(description = "Categoría")           @RequestParam(required = false) String categoria,
            @Parameter(description = "Tipo de recurrencia") @RequestParam(required = false) String tipoRecurrencia,
            @Parameter(description = "Activo (true/false)") @RequestParam(required = false) String activo,
            @Parameter(description = "Fecha desde (ISO)")   @RequestParam(required = false) String fromDate,
            @Parameter(description = "Fecha hasta (ISO)")   @RequestParam(required = false) String toDate) {
        try {
            // Create Pageable with default sorting by fechaCreacion descending
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());

            // Create filter object
            ReservaRecurrenteFilter filter = new ReservaRecurrenteFilter();
            filter.setTab(tab);
            filter.setSearchTerm(searchTerm);
            filter.setEstadoReserva(estadoReserva);
            filter.setEstadoValidacion(estadoValidacion);
            filter.setCategoria(categoria);
            filter.setTipoRecurrencia(tipoRecurrencia);
            if (activo != null && !activo.isEmpty()) {
                filter.setActivo(Boolean.parseBoolean(activo));
            }
            if (fromDate != null && !fromDate.isEmpty()) {
                filter.setFromDate(LocalDate.parse(fromDate.substring(0, 10)));
            }
            if (toDate != null && !toDate.isEmpty()) {
                filter.setToDate(LocalDate.parse(toDate.substring(0, 10)));
            }
            Page<ReservaRecurrenteDto> reservasPage = reservaRecurrenteService.obtenerReservasConFiltros(filter, pageable);
            return Response.ok().setPayload(reservasPage);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener todas las reservas")
    @GetMapping("/all")
    public Response obtenerTodasReservas(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaRecurrenteDto> reservasPage = reservaRecurrenteService.obtenerReservas(pageable);
                return Response.ok().setPayload(reservasPage);
            } else {
                List<ReservaRecurrenteDto> reservasDtos = reservaRecurrenteService.obtenerReservas();
                return Response.ok().setPayload(reservasDtos);
            }
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener reserva recurrente por UUID")
    @GetMapping("/{uuid}")
    public Response obtenerReservaRecurrentePorUuid(
            @Parameter(description = "UUID de la reserva recurrente")
            @PathVariable("uuid") String uuid) {
        try {
            ReservaRecurrenteDto reservaRecurrenteDto = reservaRecurrenteService.findByUuid(uuid);
            return Response.ok().setPayload(reservaRecurrenteDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva recurrente no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Verificar y actualizar reservas expiradas")
    @PostMapping("/verificar-expiradas")
    public Response verificarReservasExpiradas() {
        try {
            List<ReservaRecurrenteDto> updatedReservas = reservaRecurrenteService.checkAndUpdateExpiredReservations();
            return Response.ok().setPayload(updatedReservas);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al verificar reservas expiradas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reserva recurrente por UUID de reserva")
    @GetMapping("/reserva/{reservaUuid}")
    public Response buscarPorReservaUuid(
            @Parameter(description = "UUID de la reserva principal")
            @PathVariable("reservaUuid") String reservaUuid) {
        try {
            ReservaRecurrenteDto reservaRecurrenteDto = reservaRecurrenteService.findByReservaUuid(reservaUuid);
            return Response.ok().setPayload(reservaRecurrenteDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva recurrente no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas recurrentes por tipo")
    @GetMapping("/tipo/{tipoRecurrencia}")
    public Response buscarPorTipoRecurrencia(
            @Parameter(description = "Tipo de recurrencia")
            @PathVariable("tipoRecurrencia") String tipoRecurrencia) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findByTipoRecurrencia(tipoRecurrencia);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por tipo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas recurrentes por estado activo")
    @GetMapping("/activas/{activo}")
    public Response buscarPorActivo(
            @Parameter(description = "Estado activo")
            @PathVariable("activo") Boolean activo) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findByActivo(activo);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por estado activo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas en una fecha específica")
    @GetMapping("/activas/fecha/{fecha}")
    public Response buscarActivasEnFecha(
            @Parameter(description = "Fecha específica (formato: yyyy-MM-dd)")
            @PathVariable("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findActivasEnFecha(fecha);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas activas en fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas por campo y fecha")
    @GetMapping("/activas/campo/{campoUuid}/fecha/{fecha}")
    public Response buscarActivasPorCampoYFecha(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("campoUuid") String campoUuid,
            @Parameter(description = "Fecha específica (formato: yyyy-MM-dd)")
            @PathVariable("fecha") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findActivasEnFechaPorCampo(campoUuid, fecha);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas activas por campo y fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas vigentes")
    @GetMapping("/activas/vigentes")
    public Response buscarActivasVigentes() {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findActivasVigentes();
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas activas vigentes: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas por día de la semana")
    @GetMapping("/activas/dia-semana/{diaSemana}")
    public Response buscarActivasPorDiaSemana(
            @Parameter(description = "Día de la semana (1-7, donde 1=Lunes)")
            @PathVariable("diaSemana") String diaSemana,
            @Parameter(description = "Fecha específica (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fecha) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findActivasPorDiaSemana(diaSemana, fecha);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por día de semana: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por vencer")
    @GetMapping("/por-vencer")
    public Response buscarPorVencer(
            @Parameter(description = "Fecha límite (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaLimite) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findPorVencer(fechaLimite);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por vencer: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas recurrentes por campo deportivo (paginado)")
    @GetMapping("/campo/{campoUuid}")
    public Response buscarPorCampoDeportivo(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("campoUuid") String campoUuid,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReservaRecurrenteDto> reservasPage = reservaRecurrenteService.findByCampoDeportivoUuid(campoUuid, pageable);
            return Response.ok().setPayload(reservasPage);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por campo: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por categoría")
    @GetMapping("/categoria/{categoria}")
    public Response buscarPorCategoria(
            @Parameter(description = "Categoría de la reserva")
            @PathVariable("categoria") String categoria) {
        try {
            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findByCategoria(categoria);
            return Response.ok().setPayload(reservasRecurrentes);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por categoría: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Buscar reservas sin tablero asignado")
//    @GetMapping("/sin-tablero")
//    public Response buscarSinTablero() {
//        try {
//            List<ReservaRecurrenteDto> reservasRecurrentes = reservaRecurrenteService.findSinTablero();
//            return Response.ok().setPayload(reservasRecurrentes);
//        } catch (Exception ex) {
//            return Response.notFound().setPayload("Error al buscar reservas sin tablero: " + ex.getMessage());
//        }
//    }

    @Operation(summary = "Crear nueva reserva recurrente")
    @PostMapping()
    public Response crearReservaRecurrente(@RequestBody ReservaRecurrenteDto reservaRecurrenteDto) {
        try {
            ReservaRecurrenteDto nuevaReservaRecurrente = reservaRecurrenteService.crearReservaRecurrente(reservaRecurrenteDto);
            return Response.ok().setPayload(nuevaReservaRecurrente);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear reserva recurrente: " + ex.getMessage());
        }
    }

    @Operation(summary = "Actualizar reserva recurrente")
    @PutMapping()
    public Response actualizarReservaRecurrente(@RequestBody ReservaRecurrenteDto reservaRecurrenteDto) {
        try {
            ReservaRecurrenteDto reservaActualizada = reservaRecurrenteService.actualizarReservaRecurrente(reservaRecurrenteDto);
            return Response.ok().setPayload(reservaActualizada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al actualizar reserva recurrente: " + ex.getMessage());
        }
    }

    @Operation(summary = "Activar reserva recurrente")
    @PutMapping("/{uuid}/activar")
    public Response activarReservaRecurrente(
            @Parameter(description = "UUID de la reserva recurrente")
            @PathVariable("uuid") String uuid) {
        try {
            ReservaRecurrenteDto reservaActivada = reservaRecurrenteService.activarReservaRecurrente(uuid);
            return Response.ok().setPayload(reservaActivada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al activar reserva recurrente: " + ex.getMessage());
        }
    }

    @Operation(summary = "Desactivar reserva recurrente")
    @PutMapping("/{uuid}/desactivar")
    public Response desactivarReservaRecurrente(
            @Parameter(description = "UUID de la reserva recurrente")
            @PathVariable("uuid") String uuid) {
        try {
            ReservaRecurrenteDto reservaDesactivada = reservaRecurrenteService.desactivarReservaRecurrente(uuid);
            return Response.ok().setPayload(reservaDesactivada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al desactivar reserva recurrente: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar reserva recurrente")
    @DeleteMapping("/{uuid}")
    public Response eliminarReservaRecurrente(
            @Parameter(description = "UUID de la reserva recurrente")
            @PathVariable("uuid") String uuid) {
        try {
            reservaRecurrenteService.eliminarReserva(uuid);
            return Response.ok().setPayload("Reserva recurrente eliminada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar reserva recurrente: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Contar reservas activas por campo")
//    @GetMapping("/contador/activas/campo/{campoUuid}")
//    public Response contarActivasPorCampo(
//            @Parameter(description = "UUID del campo deportivo")
//            @PathVariable("campoUuid") String campoUuid) {
//        try {
//            Long cantidad = reservaRecurrenteService.countActivasByCampoDeportivo(campoUuid);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar reservas activas: " + ex.getMessage());
//        }
//    }
//    @Operation(summary = "Contar reservas activas vigentes")
//    @GetMapping("/contador/activas/vigentes")
//    public Response contarActivasVigentes() {
//        try {
//            Long cantidad = reservaRecurrenteService.countActivasVigentes();
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar reservas activas vigentes: " + ex.getMessage());
//        }
//    }
}
