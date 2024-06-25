package com.gamq.licencias.controller;

import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.service.canchasService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;


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

}
