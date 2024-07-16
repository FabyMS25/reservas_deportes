package com.gamq.licencias.controller;


import com.gamq.licencias.dto.encargadoComplejoDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.encargadoCanchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encargado")
public class encargadoCanchaController {

    @Autowired
    encargadoCanchaService encargadoCanchaService;

    @GetMapping()
    public Response obtenerEncargados() {
        try {
            List<encargadoComplejoDto> encargadoComplejoDtos = encargadoCanchaService.obtenerEncargadosCanchas();
            return Response.ok().setPayload(encargadoComplejoDtos);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @GetMapping("/{uuid}")
    public Response buscarEncargadoPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            encargadoComplejoDto encargadoComplejoDto = encargadoCanchaService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearEncargado(@RequestBody encargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCanchaService.crearEncargadoCancha(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarEncargado(@RequestBody encargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCanchaService.actualizarEncargadoCancha(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
