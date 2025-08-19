package com.gamq.deportes.controller;


import com.gamq.deportes.dto.ReservasProgramadasDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ReservasProgramadasService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservasprogramadas")
public class ReservasProgramadasController {
    @Autowired
    private ReservasProgramadasService reservasProgramadasService;

    @GetMapping()
    public Response obtenerReservasProgramadas() {
        try{
            List<ReservasProgramadasDto> ReservasProgramadasDtos =  reservasProgramadasService.obtenerReservasProgramadas();
            return Response.ok().setPayload(ReservasProgramadasDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarReservasPorUuid( @PathVariable("uuid") String uuidReservasProgramadas) {
        try{
            ReservasProgramadasDto ReservasProgramadasDto = reservasProgramadasService.findByUuid(uuidReservasProgramadas);
            return Response.ok().setPayload(ReservasProgramadasDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }


    @PostMapping()
    public Response crearReserva(@RequestBody ReservasProgramadasDto ReservasProgramadasDto) {
        try{
            return Response.ok().setPayload(reservasProgramadasService.crearReservasProgramadas(ReservasProgramadasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarReserva(@RequestBody ReservasProgramadasDto ReservasProgramadasDto) {
        try{
            return Response.ok().setPayload(reservasProgramadasService.actualizarReservasProgramadas(ReservasProgramadasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    /*@DeleteMapping("/{uuid}")
    public Response deleteField(@Parameter(description = "Uuid para eliminar cancha")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(ReservasProgramadasService.deleteField(uuid));
    }*/
}
