package com.gamq.deportes.controller;


import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.HorarioFijoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ScheduleOperationalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/horarioFijo")
@Tag(name = "Horario Fijo ", description = "Horarios fijo para toma de Servicios GAMQ ")
public class ScheduleOperationalController {

    @Autowired
    ScheduleOperationalService horarioService;
    @Operation(summary = "Listar Horarios Fijos")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('/settings/fields')")
    public Response obtenerHorarios() {
        try{
            List<HorarioFijoDto> horarioDtos =  horarioService.obtenerHorarios();
            return Response.ok().setPayload(horarioDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Horarios Fijos por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('/settings/fields')")
    public Response buscarHorarioPorUuid( @PathVariable("uuid") String uuidHorario) {
        try{
            HorarioFijoDto horarioDto = horarioService.findByUuid(uuidHorario);
            return Response.ok().setPayload(horarioDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Horarios Fijos")
    @PostMapping()
    @PreAuthorize("@permissions.has('/settings/fields','CREAR')")
    public Response crearHorario(@RequestBody HorarioFijoDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.crearHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Horarios Fijos")
    @PutMapping()
    @PreAuthorize("@permissions.has('/settings/fields','ACTUALIZAR')")
    public Response actualizarHorario(@RequestBody HorarioFijoDto horarioDto) {
        try{
            return Response.ok().setPayload(horarioService.actualizarHorario(horarioDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Horarios Fijos por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('/settings/fields','ANULAR')")
    public Response deleteSchedule(@Parameter(description = "Uuid para eliminar HorarioFijo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(horarioService.delete(uuid));
    }
}

