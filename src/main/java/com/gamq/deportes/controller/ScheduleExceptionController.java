package com.gamq.deportes.controller;


import com.gamq.deportes.dto.HorarioExcepcionDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ScheduleExceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarioExcepcion")
@Tag(name = "Horarios ", description = "Horarios para toma de Servicios GAMQ ")
public class ScheduleExceptionController {

    @Autowired
    ScheduleExceptionService horarioService;
    @Operation(summary = "Listar Horarios")
    @GetMapping()
    public Response obtenerHorarios() {
        try{
            List<HorarioExcepcionDto> horarioDtos =  horarioService.obtenerHorarios();
            return Response.ok().setPayload(horarioDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Horario por uuid")
    @GetMapping("/{uuid}")
    public Response buscarHorarioPorUuid( @PathVariable("uuid") String uuidHorario) {
        try{
            HorarioExcepcionDto horarioDto = horarioService.findByUuid(uuidHorario);
            return Response.ok().setPayload(horarioDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Horario")
    @PostMapping()
    public Response crearHorario(@RequestBody HorarioExcepcionDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.crearHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Horario")
    @PutMapping()
    public Response actualizarHorario(@RequestBody HorarioExcepcionDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.actualizarHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Horario por uuid ")
    @DeleteMapping("/{uuid}")
    public Response deleteSchedule(@Parameter(description = "Uuid para eliminar HorarioFijo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(horarioService.delete(uuid));
    }
}
