package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.dto.ReservaPeriodoFilter;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ApiService;
import com.gamq.deportes.service.ReservaPeriodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/reservas-periodo")
@Tag(name = "Reservas por Periodo", description = "API para Reservas Por Periodo")
public class ReservaPeriodoController {

    @Autowired
    private ReservaPeriodoService reservaPeriodoService;


    @Operation(summary = "Obtener todas las reservas por Periodo con filtros y paginación")
    @GetMapping()
    @PreAuthorize("@permissions.has('/reservations/schools','VER')")
    public Response obtenerReservasPeriodo(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "true") boolean revisarEstados,
            @Parameter(description = "Pestaña seleccionada (0=Todas,1=Pendientes,2=Pagadas,3=Vencidas,4=Este Mes)")
            @RequestParam(required = false) Integer tab,
            @Parameter(description = "Término de búsqueda") @RequestParam(required = false) String searchTerm,
            @Parameter(description = "Estado de reserva")   @RequestParam(required = false) String estadoReserva,
            @Parameter(description = "Estado de validación")@RequestParam(required = false) String estadoValidacion,
            @Parameter(description = "Categoría")           @RequestParam(required = false) String categoria,
            @Parameter(description = "Escuela (UUID)")      @RequestParam(required = false) String escuelaFiltro,
            @Parameter(description = "Período (YYYY-MM)")   @RequestParam(required = false) String periodoFiltro) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
            ReservaPeriodoFilter filter = new ReservaPeriodoFilter();
            filter.setRevisarEstados(revisarEstados);
            filter.setTab(tab);
            filter.setSearchTerm(searchTerm);
            filter.setEstadoReserva(estadoReserva);
            filter.setEstadoValidacion(estadoValidacion);
            filter.setCategoria(categoria);
            filter.setEscuelaFiltro(escuelaFiltro);
            filter.setPeriodoFiltro(periodoFiltro);
            Page<ReservaPeriodoDto> reservasPage = reservaPeriodoService.obtenerReservasConFiltros(filter, pageable);
            return Response.ok().setPayload(reservasPage);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
//            return Response.serverError().setPayload("Error al obtener reservas por período: " + ex.getMessage());
        }
    }

    @Operation(summary = "Obtener todas las reservas por Periodo sin paginación")
    @GetMapping("/all")
    @PreAuthorize("@permissions.has('/reservations/schools','VER')")
    public Response obtenerTodasReservasPeriodo() {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.obtenerTodos();
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener reserva-periodo por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.child('periodo','VER',#p0)")
    public Response buscarReservaPeriodoPorUuid(@PathVariable("uuid") String uuid) {
        try {
            ReservaPeriodoDto reservaPeriodoDto = reservaPeriodoService.findByUuid(uuid);
            return Response.ok().setPayload(reservaPeriodoDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener reserva-periodo por RservaUuid global")
    @GetMapping("/reserva/{reservaUuid}")
    @PreAuthorize("@permissions.reservation('VER',#p0)")
    public Response buscarReservaPeriodoPorReserva(@PathVariable("reservaUuid") String reservaUuid) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.findByReservaUuid(reservaUuid));
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @GetMapping("/estudiante/{uuidEstudiante}/historial")
    @PreAuthorize("@permissions.has('/students','VER')")
    public Response historialPorEstudiante(@PathVariable("uuidEstudiante") String uuidEstudiante) {
        return Response.ok().setPayload(reservaPeriodoService.historialPorEstudiante(uuidEstudiante));
    }
    @Operation(summary = "Obtener reserva-periodo por Estudiante uuid")
    @GetMapping("/estudiante/{uuidEstudiante}")
    @PreAuthorize("@permissions.has('/reservations/schools','VER')")
    public Response buscarReservasPeriodoPorEstudiante(@PathVariable("uuidEstudiante") String uuidEstudiante) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByEstudianteUuid(uuidEstudiante);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener reserva-periodo por periodo")
    @GetMapping("/periodo/{periodo}")
    @PreAuthorize("@permissions.has('/reservations/schools','VER')")
    public Response buscarReservasPeriodoPorPeriodo(@PathVariable("periodo") String periodo) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByPeriodo(periodo);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Obtener reserva-periodo por Contribuyente")
    @GetMapping("/contribuyente/{uuidContribuyente}")
    @PreAuthorize("@permissions.has('/reservations/schools','VER') and (@permissions.staff() or @permissions.self(#p0))")
    public Response buscarReservasPeriodoPorContribuyente(@PathVariable("uuidContribuyente") String uuidContribuyente) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByContribuyenteUuid(uuidContribuyente);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener reserva-periodo por Organizacion uuid")
    @GetMapping("/organizacion/{uuidOrganizacion}")
    @PreAuthorize("@permissions.has('/reservations/schools','VER') and (@permissions.staff() or @permissions.represents(#p0))")
    public Response buscarReservasPeriodoPorOrganizacion(@PathVariable("uuidOrganizacion") String uuidOrganizacion) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByOrganizacionUuid(uuidOrganizacion);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear reserva-periodo ")
    @PostMapping()
    @PreAuthorize("@permissions.childBody('periodo','CREAR',#p0)")
    public Response crearReservaPeriodo(@RequestBody ReservaPeriodoDto reservaPeriodoDto) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.crearReservaPeriodo(reservaPeriodoDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar reserva-periodo ")
    @PutMapping()
    @PreAuthorize("@permissions.childBody('periodo','ACTUALIZAR',#p0)")
    public Response actualizarReservaPeriodo(@RequestBody ReservaPeriodoDto reservaPeriodoDto) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.actualizarReservaPeriodo(reservaPeriodoDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar reserva-periodo por uuid ")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.child('periodo','ANULAR',#p0)")
    public Response eliminarReservaPeriodo(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.eliminarReservaPeriodo(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
