package com.gamq.deportes.controller;

import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.dto.CampoDeportivoDto;
import com.gamq.deportes.service.CampoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/api/campoDeportivo")
public class canchasController {
    @Autowired
    private CampoService canchasService;

    @GetMapping()
    public Response obtenerCanchas() {
        try{
            List<CampoDeportivoDto> canchasDtos =  canchasService.obtenerCampos();
            return Response.ok().setPayload(canchasDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            CampoDeportivoDto canchasDto = canchasService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(canchasDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/complejo/{uuid}")
    public Response buscarCanchasPorComplejo(@Parameter(description = "Uuid de Complejo para busqueda de Canchas")
                                             @PathVariable("uuid") String uuidComplejo) {
        return Response.ok().setPayload(canchasService.findCampoByComplejo(uuidComplejo));
    }
    @PostMapping()
    public Response crearConstruccion(@RequestBody CampoDeportivoDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.crearCampo(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody CampoDeportivoDto canchasDto) {
        try{
            return Response.ok().setPayload(canchasService.actualizarCampo(canchasDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteField(@Parameter(description = "Uuid para eliminar cancha")
                                 @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(canchasService.eliminarCampos(uuid));
    }
}
