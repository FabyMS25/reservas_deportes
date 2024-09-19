package com.gamq.licencias.controller;

import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.service.canchasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/canchas")
public class canchasController {
    @Autowired
    private canchasService canchasService;

    @GetMapping()
    public Response obtenerCanchas() {
        try{
            List<canchasDto> canchasDtos =  canchasService.obtenerCanchas();
            return Response.ok().setPayload(canchasDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            canchasDto canchasDto = canchasService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(canchasDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/complejo/{uuid}")
    public Response buscarCanchasPorComplejo(@Parameter(description = "Uuid de Complejo para busqueda de Canchas")
                                             @PathVariable("uuid") String uuidComplejo) {
        return Response.ok().setPayload(canchasService.findCanchasByComplejo(uuidComplejo));
    }
    @PostMapping()
    public Response crearConstruccion(@RequestBody canchasDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.crearCancha(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody canchasDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.actualizarCancha(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteField(@Parameter(description = "Uuid para eliminar cancha")
                                 @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(canchasService.deleteField(uuid));
    }
}
