package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ReservaPeriodoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservaPeriodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservas-periodo")
public class ReservaPeriodoController {

    @Autowired
    private ReservaPeriodoService reservaPeriodoService;

    @GetMapping()
    public Response obtenerReservasPeriodo() {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.obtenerTodos();
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarReservaPeriodoPorUuid(@PathVariable("uuid") String uuid) {
        try {
            ReservaPeriodoDto reservaPeriodoDto = reservaPeriodoService.findByUuid(uuid);
            return Response.ok().setPayload(reservaPeriodoDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/reserva/{reservaUuid}")
    public Response buscarReservaPeriodoPorReserva(@PathVariable("reservaUuid") String reservaUuid) {
        try {
            ReservaPeriodoDto reservaPeriodoDto = reservaPeriodoService.findByReservaUuid(reservaUuid);
            return Response.ok().setPayload(reservaPeriodoDto);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/periodo/{periodo}")
    public Response buscarReservasPeriodoPorPeriodo(@PathVariable("periodo") String periodo) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByPeriodo(periodo);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }


    @GetMapping("/contribuyente/{uuidContribuyente}")
    public Response buscarReservasPeriodoPorContribuyente(@PathVariable("uuidContribuyente") String uuidContribuyente) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByContribuyenteUuid(uuidContribuyente);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/organizacion/{uuidOrganizacion}")
    public Response buscarReservasPeriodoPorOrganizacion(@PathVariable("uuidOrganizacion") String uuidOrganizacion) {
        try {
            List<ReservaPeriodoDto> reservasPeriodo = reservaPeriodoService.findByOrganizacionUuid(uuidOrganizacion);
            return Response.ok().setPayload(reservasPeriodo);
        } catch (Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearReservaPeriodo(@RequestBody ReservaPeriodoDto reservaPeriodoDto) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.crearReservaPeriodo(reservaPeriodoDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarReservaPeriodo(@RequestBody ReservaPeriodoDto reservaPeriodoDto) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.actualizarReservaPeriodo(reservaPeriodoDto));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @DeleteMapping("/{uuid}")
    public Response eliminarReservaPeriodo(@PathVariable("uuid") String uuid) {
        try {
            return Response.ok().setPayload(reservaPeriodoService.eliminarReservaPeriodo(uuid));
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}