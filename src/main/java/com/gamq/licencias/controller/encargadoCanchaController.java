package com.gamq.licencias.controller;


import com.gamq.licencias.dto.canchasDto;
import com.gamq.licencias.dto.encargadoCanchaDto;
import com.gamq.licencias.repository.encargadoCanchaRepository;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.canchasService;
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
    public Response obtenerCanchas() {
        try{
            List<encargadoCanchaDto> encargadoCanchaDtos =  encargadoCanchaService.obtenerEncargadosCanchas();
            return Response.ok().setPayload(encargadoCanchaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            encargadoCanchaDto encargadoCanchaDto = encargadoCanchaService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(encargadoCanchaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearConstruccion(@RequestBody encargadoCanchaDto encargadoCanchaDto) {
        try{
            return Response.ok().setPayload(encargadoCanchaService.crearEncargadoCancha(encargadoCanchaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody encargadoCanchaDto encargadoCanchaDto) {
        try{
            return Response.ok().setPayload(encargadoCanchaService.actualizarEncargadoCancha(encargadoCanchaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
