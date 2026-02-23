package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaPuntualDto;
import com.gamq.deportes.dto.ReservaPuntualFilter;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ApiService;
import com.gamq.deportes.service.ReservaPuntualService;
import com.gamq.deportes.service.ReservaService;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-puntuales")
@Tag(name = "Reservas Puntuales", description = "API para gestión de reservas puntuales")
public class ReservaPuntualController {

    @Autowired
    private ReservaPuntualService reservaPuntualService;

    @Autowired
    private ApiService ruatService;

    @Autowired
    private ReservaService reservaService;

    @Operation(summary = "Verificar y actualizar reservas expiradas")
    @PostMapping("/verificar-expiradas")
    public Response verificarReservasExpiradas(
            @Parameter(description = "Horas de timeout para expiración") @RequestParam int horasTimeout) {
        try {

            List<ReservaPuntualDto> updatedReservas = reservaPuntualService.checkAndUpdateExpiredReservations(horasTimeout);
            return Response.ok().setPayload(updatedReservas);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al verificar reservas expiradas: " + ex.getMessage());
        }
    }


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
            @Parameter(description = "Fecha desde (ISO)")   @RequestParam(required = false) String fromDate,
            @Parameter(description = "Fecha hasta (ISO)")   @RequestParam(required = false) String toDate) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            ReservaPuntualFilter filter = new ReservaPuntualFilter();
            filter.setTab(tab);
            filter.setSearchTerm(searchTerm);
            filter.setEstadoReserva(estadoReserva);
            filter.setEstadoValidacion(estadoValidacion);
            filter.setCategoria(categoria);
            if (fromDate != null && !fromDate.isEmpty()) {
                filter.setFromDate(LocalDate.parse(fromDate.substring(0, 10)));
            }
            if (toDate != null && !toDate.isEmpty()) {
                filter.setToDate(LocalDate.parse(toDate.substring(0, 10)));
            }
            Page<ReservaPuntualDto> reservasPage = reservaPuntualService.obtenerReservasConFiltros(filter, pageable);
            return Response.ok().setPayload(reservasPage);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas: " + ex.getMessage());
        }
    }

    @GetMapping("/all")
    public Response obtenerTodasReservas(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            if (page >= 0 && size > 0) {
                Pageable pageable = PageRequest.of(page, size);
                Page<ReservaPuntualDto> reservasPage = reservaPuntualService.obtenerReservas(pageable);
                return Response.ok().setPayload(reservasPage);
            } else {
                List<ReservaPuntualDto> reservasDtos = reservaPuntualService.obtenerReservas();
                return Response.ok().setPayload(reservasDtos);
            }
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas: " + ex.getMessage());
        }
    }


    @Operation(summary = "Obtener reserva puntual por UUID")
    @GetMapping("/{uuid}")
    public Response obtenerReservaPuntualPorUuid(
            @Parameter(description = "UUID de la reserva puntual")
            @PathVariable("uuid") String uuid) {
        try {
            ReservaPuntualDto reservaPuntualDto = reservaPuntualService.findByUuid(uuid);
            return Response.ok().setPayload(reservaPuntualDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva puntual no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reserva puntual por UUID de reserva")
    @GetMapping("/reserva/{reservaUuid}")
    public Response buscarPorReservaUuid(
            @Parameter(description = "UUID de la reserva principal")
            @PathVariable("reservaUuid") String reservaUuid) {
        try {
            ReservaPuntualDto reservaPuntualDto = reservaPuntualService.findByReservaUuid(reservaUuid);
            return Response.ok().setPayload(reservaPuntualDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Reserva puntual no encontrada: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas puntuales por fecha de uso")
    @GetMapping("/fecha-uso/{fechaUso}")
    public Response buscarPorFechaUso(
            @Parameter(description = "Fecha de uso (formato: yyyy-MM-dd)")
            @PathVariable("fechaUso") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaUso) {
        try {
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findByFechaUso(fechaUso);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas puntuales por rango de fechas")
    @GetMapping("/fecha-uso")
    public Response buscarPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin) {
        try {
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findByFechaUsoBetween(fechaInicio, fechaFin);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por rango de fechas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas puntuales por campo deportivo y fecha")
    @GetMapping("/campo/{campoUuid}/fecha/{fechaUso}")
    public Response buscarPorCampoYFecha(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("campoUuid") String campoUuid,
            @Parameter(description = "Fecha de uso (formato: yyyy-MM-dd)")
            @PathVariable("fechaUso") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaUso) {
        try {
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findByCampoDeportivoAndFechaUso(campoUuid, fechaUso);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por campo y fecha: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas activas por campo y fecha")
    @GetMapping("/activas/campo/{campoUuid}/fecha/{fechaUso}")
    public Response buscarActivasPorCampoYFecha(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("campoUuid") String campoUuid,
            @Parameter(description = "Fecha de uso (formato: yyyy-MM-dd)")
            @PathVariable("fechaUso") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaUso) {
        try {
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findReservasActivasPorCampoYFecha(campoUuid, fechaUso);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas activas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Verificar conflictos de horario")
    @GetMapping("/conflictos")
    public Response verificarConflictos(
            @Parameter(description = "UUID del campo deportivo")
            @RequestParam String campoUuid,
            @Parameter(description = "Fecha de uso (formato: yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaUso,
            @Parameter(description = "Hora de inicio (formato: HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaInicio,
            @Parameter(description = "Hora de fin (formato: HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime horaFin,
            @Parameter(description = "UUID de reserva a excluir")
            @RequestParam(required = false) String excludeReservaUuid) {
        try {
            boolean existeConflicto = reservaPuntualService.existeReservaConflictiva(campoUuid, fechaUso, horaInicio, horaFin, excludeReservaUuid);
            return Response.ok().setPayload(existeConflicto);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al verificar conflictos: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener reservas puntuales por campo deportivo (paginado)")
    @GetMapping("/campo/{campoUuid}")
    public Response buscarPorCampoDeportivo(
            @Parameter(description = "UUID del campo deportivo")
            @PathVariable("campoUuid") String campoUuid,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ReservaPuntualDto> reservasPage = reservaPuntualService.findByCampoDeportivoUuid(campoUuid, pageable);
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
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findByCategoria(categoria);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por categoría: " + ex.getMessage());
        }
    }

    @Operation(summary = "Buscar reservas por rango de personas")
    @GetMapping("/personas")
    public Response buscarPorRangoPersonas(
            @Parameter(description = "Cantidad mínima de personas")
            @RequestParam Integer cantidadMin,
            @Parameter(description = "Cantidad máxima de personas")
            @RequestParam Integer cantidadMax) {
        try {
            List<ReservaPuntualDto> reservasPuntuales = reservaPuntualService.findByCantidadPersonasBetween(cantidadMin, cantidadMax);
            return Response.ok().setPayload(reservasPuntuales);
        } catch (Exception ex) {
            return Response.notFound().setPayload("Error al buscar reservas por rango de personas: " + ex.getMessage());
        }
    }

    @Operation(summary = "Crear nueva reserva puntual")
    @PostMapping()
    public Response crearReservaPuntual(@RequestBody ReservaPuntualDto reservaPuntualDto) {
        try {
            ReservaPuntualDto nuevaReservaPuntual = reservaPuntualService.crearReservaPuntual(reservaPuntualDto);
            return Response.ok().setPayload(nuevaReservaPuntual);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al crear reserva puntual: " + ex.getMessage());
        }
    }

    @Operation(summary = "Actualizar reserva puntual")
    @PutMapping()
    public Response actualizarReservaPuntual(@RequestBody ReservaPuntualDto reservaPuntualDto) {
        try {
            ReservaPuntualDto reservaActualizada = reservaPuntualService.actualizarReservaPuntual(reservaPuntualDto);
            return Response.ok().setPayload(reservaActualizada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al actualizar reserva puntual: " + ex.getMessage());
        }
    }

    @Operation(summary = "Registrar uso real de la reserva")
    @PutMapping("/{uuid}/registrar-uso")
    public Response registrarUsoReal(
            @Parameter(description = "UUID de la reserva puntual")
            @PathVariable("uuid") String uuid,
            @Parameter(description = "Fecha y hora real de uso (formato: yyyy-MM-dd HH:mm)")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime fechaUsoReal) {
        try {
            ReservaPuntualDto reservaActualizada = reservaPuntualService.registrarUsoReal(uuid, fechaUsoReal);
            return Response.ok().setPayload(reservaActualizada);
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al registrar uso real: " + ex.getMessage());
        }
    }

    @Operation(summary = "Eliminar reserva puntual")
    @DeleteMapping("/{uuid}")
    public Response eliminarReservaPuntual(
            @Parameter(description = "UUID de la reserva puntual")
            @PathVariable("uuid") String uuid) {
        try {
            reservaPuntualService.eliminarReserva(uuid);
            return Response.ok().setPayload("Reserva puntual eliminada exitosamente");
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al eliminar reserva puntual: " + ex.getMessage());
        }
    }

//    @Operation(summary = "Contar reservas por campo y fecha")
//    @GetMapping("/contador/campo/{campoUuid}/fecha/{fechaUso}")
//    public Response contarPorCampoYFecha(
//            @Parameter(description = "UUID del campo deportivo")
//            @PathVariable("campoUuid") String campoUuid,
//            @Parameter(description = "Fecha de uso (formato: yyyy-MM-dd)")
//            @PathVariable("fechaUso") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaUso) {
//        try {
//            Long cantidad = reservaPuntualService.countByCampoDeportivoAndFechaUso(campoUuid, fechaUso);
//            return Response.ok().setPayload(cantidad);
//        } catch (Exception ex) {
//            return Response.serverError().setPayload("Error al contar reservas: " + ex.getMessage());
//        }
//    }
}