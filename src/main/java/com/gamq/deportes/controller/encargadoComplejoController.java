package com.gamq.deportes.controller;


import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.EncargadoComplejoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.EncargadoComplejoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/encargado")
@Tag(name = "Encargados", description = "Encargados de Canchas o complejos en GAMQ ")
public class encargadoComplejoController {

    @Autowired
    EncargadoComplejoService encargadoCcomplejoService;
    @Operation(summary = "Listar Encargados")
    @GetMapping()
    @PreAuthorize("@permissions.has('Encargados','VER')")
    public Response obtenerEncargados() {
        try {
            List<EncargadoComplejoDto> encargadoComplejoDtos = encargadoCcomplejoService.obtenerEncargadosComplejos();
            return Response.ok().setPayload(encargadoComplejoDtos);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Encargado por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Encargados','VER')")
    public Response buscarEncargadoPorUuid( @PathVariable("uuid") String uuid) {
        try{
            EncargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUuid(uuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Encargado por userUuid")
    @GetMapping("user/{userUuid}")
    @PreAuthorize("@permissions.self(#p0) or @permissions.has('Encargados','VER')")
    public Response buscarEncargadoPorUserUuid( @PathVariable("userUuid") String userUuid) {
        try{
            EncargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUserUuid(userUuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Encargado")
    @PostMapping()
    @PreAuthorize("@permissions.has('Encargados','CREAR')")
    public Response crearEncargado(@RequestBody EncargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.crearEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Encargado")
    @PutMapping()
    @PreAuthorize("@permissions.has('Encargados','ACTUALIZAR')")
    public Response actualizarEncargado(@RequestBody EncargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.actualizarEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Encargado por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Encargados','ANULAR')")
    public Response deleteEmployee(@Parameter(description = "Uuid para eliminar encargado")
                                 @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(encargadoCcomplejoService.delete(uuid));
    }
}
