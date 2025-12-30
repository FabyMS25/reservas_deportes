package com.gamq.deportes.controller;

import com.gamq.deportes.dto.PagoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/api/pagos")
@Tag(name = "Pagos", description = "API para Pagos organizaciones")
public class pagosController {

    @Autowired
    PagoService pagoService;
    @Operation(summary = "Obtener todos los Pagos")
    @GetMapping()
    public Response obtenerPagos() {
        try{
            List<PagoDto> pagoDtos =  pagoService.obtenerPagos();
            return Response.ok().setPayload(pagoDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener Pago por uuid")
    @GetMapping("/{uuid}")
    public Response buscarPagoPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            PagoDto pagoDto = pagoService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(pagoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener Pago por intervalo de fechas")
    @GetMapping("/fechas")
    public Response buscarPagoPorFechaPago( @RequestParam(name = "fechaIni") @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaIni,
                                                  @RequestParam(name = "fechaFin") @DateTimeFormat(pattern = "dd/MM/yyyy") Date fechaFin) {
        try{
            List<PagoDto> pagosDtos = pagoService.buscarPorFechaPago(fechaIni, fechaFin);
            return Response.ok().setPayload(pagosDtos);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Crear Pago")
    @PostMapping()
    public Response crearPago(@RequestBody PagoDto pagoDto) {
        try{
            return Response.ok().setPayload(pagoService.crearPago(pagoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Pago")
    @PutMapping()
    public Response actualizarPago(@RequestBody PagoDto pagoDto) {
        try{
            return Response.ok().setPayload(pagoService.actualizarPago(pagoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }


}
