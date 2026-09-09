package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.ComplejoDeportivoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ComplejoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/complejos")
@Tag(name = "Complejos", description = "Complejos del GAMQ ")
public class complejoController {
    @Autowired
    private ComplejoService complejoService;
    @Operation(summary = "Listar Complejos")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('Complejos Deportivos')")
    public Response obtenercomplejos() {
        try{
            List<ComplejoDeportivoDto> complejoDtos =  complejoService.obtenerComplejos();
            return Response.ok().setPayload(complejoDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Complejo por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('Complejos Deportivos')")
    public Response buscarcomplejoPorUuid( @PathVariable("uuid") String uuidcomplejo) {
        try{
            ComplejoDeportivoDto complejoDto = complejoService.findByUuid(uuidcomplejo);
            return Response.ok().setPayload(complejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Complejo")
    @PostMapping()
    @PreAuthorize("@permissions.has('Complejos Deportivos','CREAR')")
    public Response crearcomplejo(@RequestBody ComplejoDeportivoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.crearComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Complejo")
    @PutMapping()
    @PreAuthorize("@permissions.has('Complejos Deportivos','ACTUALIZAR')")
    public Response actualizarcomplejo(@RequestBody ComplejoDeportivoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.actualizarComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Complejo por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Complejos Deportivos','ANULAR')")
    public Response deleteComplex(@Parameter(description = "Uuid para eliminar complejo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(complejoService.eliminarComplejo(uuid));
    }
}
