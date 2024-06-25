package com.gamq.licencias.controller;


import com.gamq.licencias.dto.horarioDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.horarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horario")
public class horarioController {

    @Autowired
    horarioService horarioService;

    @GetMapping()
    public Response obtenerHorarios() {
        try{
            List<horarioDto> horarioDtos =  horarioService.obtenerHorarios();
            return Response.ok().setPayload(horarioDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarHorarioPorUuid( @PathVariable("uuid") String uuidHorario) {
        try{
            horarioDto horarioDto = horarioService.findByUuid(uuidHorario);
            return Response.ok().setPayload(horarioDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearHorario(@RequestBody horarioDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.crearHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarHorario(@RequestBody horarioDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.actualizarHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
