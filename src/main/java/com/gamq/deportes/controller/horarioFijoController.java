package com.gamq.deportes.controller;


import com.gamq.deportes.dto.HorarioFijoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.horarioFijoService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarioFijo")
public class horarioFijoController {

    @Autowired
    horarioFijoService horarioService;

    @GetMapping()
    public Response obtenerHorarios() {
        try{
            List<HorarioFijoDto> horarioDtos =  horarioService.obtenerHorarios();
            return Response.ok().setPayload(horarioDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarHorarioPorUuid( @PathVariable("uuid") String uuidHorario) {
        try{
            HorarioFijoDto horarioDto = horarioService.findByUuid(uuidHorario);
            return Response.ok().setPayload(horarioDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearHorario(@RequestBody HorarioFijoDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.crearHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarHorario(@RequestBody HorarioFijoDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.actualizarHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteSchedule(@Parameter(description = "Uuid para eliminar HorarioFijo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(horarioService.delete(uuid));
    }
}
