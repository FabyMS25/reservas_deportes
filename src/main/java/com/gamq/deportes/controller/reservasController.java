package com.gamq.deportes.controller;


import com.gamq.deportes.dto.reservasDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.reservasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class reservasController {

    @Autowired
    private reservasService reservasService;

    @GetMapping()
    public Response obtenerCanchas() {
        try{
            List<reservasDto> reservasDtos =  reservasService.obtenerReservas();
            return Response.ok().setPayload(reservasDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            reservasDto reservasDto = reservasService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(reservasDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/fechas")
    public Response buscarPagoPorFechaReserva( @RequestParam(name = "fechaIni") @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaIni,
                                            @RequestParam(name = "fechaFin") @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaFin) {
        try{
            List<reservasDto> reservasDtos = reservasService.buscarPorFechaPago(fechaIni, fechaFin);
            return Response.ok().setPayload(reservasDtos);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Busca reservas por su Uuid de cancha")
    @GetMapping("/cancha/{uuid}")
    public Response buscarReservasPorCancha(@Parameter(description = "Uuid de Cancha para busqueda")
                                             @PathVariable("uuid") String uuidCancha) {
        return Response.ok().setPayload(reservasService.findReservaByCancha(uuidCancha));
    }
    @PostMapping()
    public Response crearConstruccion(@RequestBody reservasDto reservasDto) {
        try{
            return Response.ok().setPayload(reservasService.crearReserva(reservasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody reservasDto reservasDto) {
        try{
            return Response.ok().setPayload(reservasService.actualizarReserva(reservasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
