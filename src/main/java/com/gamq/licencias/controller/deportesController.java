package com.gamq.licencias.controller;
import com.gamq.licencias.dto.deporteDto;

import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.deportesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api/deportes")
public class deportesController {
    @Autowired
    deportesService deportesService;
    
    @GetMapping()
    public Response obtenerdeportes() {
        try{
            List<deporteDto> deportesDtos =  deportesService.obtenerDeportes();
            return Response.ok().setPayload(deportesDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarDeportePorUuid( @PathVariable("uuid") String uuiddeportes) {
        try{
            deporteDto deportesDto = deportesService.findByUuid(uuiddeportes);
            return Response.ok().setPayload(deportesDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearDeporte(@RequestBody deporteDto deportesDto) {
        try{
            return Response.ok().setPayload(deportesService.crearDeporte(deportesDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody deporteDto deportesDto) {
        try{
            return Response.ok().setPayload(deportesService.actualizarDeporte(deportesDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
