package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.SuperficieDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.SuperficieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/superficie")
@Tag(name = "Superficie", description = "API para gestion de superficies en las Canchas del GAMQ")
public class SuperficieController {
    @Autowired
    SuperficieService superficieService;
    @Operation(summary = "Listar las Superficies")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('Superficies')")
    public Response obtenerCategorias() {
        try{
            List<SuperficieDto> SuperficieDtos =  superficieService.obtenerSuperficies();
            return Response.ok().setPayload(SuperficieDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener Superficie por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('Superficies')")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            SuperficieDto SuperficieDto = superficieService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(SuperficieDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Superficie")
    @PostMapping()
    @PreAuthorize("@permissions.has('Superficies','CREAR')")
    public Response crearCategoria(@RequestBody SuperficieDto SuperficieDto) {
        try{
            return Response.ok().setPayload(superficieService.crearSuperficie(SuperficieDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Superficie")
    @PutMapping()
    @PreAuthorize("@permissions.has('Superficies','ACTUALIZAR')")
    public Response actualizarCategoria(@RequestBody SuperficieDto SuperficieDto) {
        try{
            return Response.ok().setPayload(superficieService.actualizarSuperficie(SuperficieDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Superficie por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Superficies','ANULAR')")
    public Response deleteSurface(@Parameter(description = "Uuid para eliminar superficie")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(superficieService.delete(uuid));
    }
}
