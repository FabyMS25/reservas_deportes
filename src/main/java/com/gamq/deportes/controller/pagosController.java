package com.gamq.deportes.controller;

import com.gamq.deportes.dto.PagoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;



@RestController
@RequestMapping("/api/pagos")
public class pagosController {

    @Autowired
    PagoService pagoService;

    @GetMapping()
    public Response obtenerPagos() {
        try{
            List<PagoDto> pagoDtos =  pagoService.obtenerPagos();
            return Response.ok().setPayload(pagoDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarPagoPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            PagoDto pagoDto = pagoService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(pagoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
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


    @PostMapping()
    public Response crearPago(@RequestBody PagoDto pagoDto) {
        try{
            return Response.ok().setPayload(pagoService.crearPago(pagoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarPago(@RequestBody PagoDto pagoDto) {
        try{
            return Response.ok().setPayload(pagoService.actualizarPago(pagoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }


}
