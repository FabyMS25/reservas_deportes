package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaTarifaDetalleDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaTarifaDetalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/reservatarifadetalles", "/api/reservation-tariff-details"})
@Tag(name = "Reserva Tarifa Detalles", description = "API para gestion de detalles historicos de tarifas de reservas")
public class ReservaTarifaDetalleController {
    @Autowired
    ReservaTarifaDetalleService reservaTarifaDetalleService;

    @Operation(summary = "Obtener detalles de tarifas de reservas")
    @GetMapping()
    public Response obtenerReservaTarifaDetalles() {
        try {
            List<ReservaTarifaDetalleDto> detalles = reservaTarifaDetalleService.obtenerReservaTarifaDetalles();
            return Response.ok().setPayload(detalles);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Obtener detalle de tarifa de reserva por uuid")
    @GetMapping("/{uuid}")
    public Response buscarReservaTarifaDetallePorUuid(@PathVariable("uuid") String uuid) {
        try {
            ReservaTarifaDetalleDto detalle = reservaTarifaDetalleService.findByUuid(uuid);
            return Response.ok().setPayload(detalle);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Obtener detalles de tarifas por uuid de reserva")
    @GetMapping("/reserva/{uuid}")
    public Response buscarPorReservaUuid(@PathVariable("uuid") String reservaUuid) {
        try {
            List<ReservaTarifaDetalleDto> detalles = reservaTarifaDetalleService.findByReservaUuid(reservaUuid);
            return Response.ok().setPayload(detalles);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Crear detalle de tarifa de reserva")
    @PostMapping()
    public Response crearReservaTarifaDetalle(@RequestBody ReservaTarifaDetalleDto reservaTarifaDetalleDto) {
        try {
            return Response.ok().setPayload(reservaTarifaDetalleService.crearReservaTarifaDetalle(reservaTarifaDetalleDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Actualizar detalle de tarifa de reserva")
    @PutMapping()
    public Response actualizarReservaTarifaDetalle(@RequestBody ReservaTarifaDetalleDto reservaTarifaDetalleDto) {
        try {
            return Response.ok().setPayload(reservaTarifaDetalleService.actualizarReservaTarifaDetalle(reservaTarifaDetalleDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Borrar detalle de tarifa de reserva por uuid")
    @DeleteMapping("/{uuid}")
    public Response deleteReservaTarifaDetalle(@Parameter(description = "Uuid para eliminar detalle de tarifa de reserva")
                                               @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(reservaTarifaDetalleService.delete(uuid));
    }
}
