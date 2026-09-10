package com.gamq.deportes.controller;


import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.ClasificadorRuatDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.exception.NotFoundException;
import com.gamq.deportes.service.ClasificadorRuatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/clasificadores")
@Tag(name = "Clasificadores", description = "Clasificadores del GAMQ ")
public class ClasificadorRuatController {
    @Autowired
    private ClasificadorRuatService clasificadorRuatService;
    @Operation(summary = "Listar las Categorias")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('/admin/classifiers')")
    public Response obtenerClasificadores() {
        try{
            List<ClasificadorRuatDto> ClasificadorRuatDtos =  clasificadorRuatService.obtenerClasificadores();
            return Response.ok().setPayload(ClasificadorRuatDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar Clasificador por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('/admin/classifiers')")
    public Response buscarClasificadorRuatPorUuid( @PathVariable("uuid") String uuidClasificadorRuat) {
        try{
            ClasificadorRuatDto ClasificadorRuatDto = clasificadorRuatService.findByUuid(uuidClasificadorRuat);
            return Response.ok().setPayload(ClasificadorRuatDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Clasificador")
    @PostMapping()
    @PreAuthorize("@permissions.has('/admin/classifiers','CREAR')")
    public Response crearClasificadorRuat(@RequestBody ClasificadorRuatDto ClasificadorRuatDto) {
        try{
            return Response.ok().setPayload(clasificadorRuatService.crearClasificador(ClasificadorRuatDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Clasificador")
    @PutMapping()
    @PreAuthorize("@permissions.has('/admin/classifiers','ACTUALIZAR')")
    public Response actualizarClasificadorRuat(@RequestBody ClasificadorRuatDto clasificadorRuatDto) {
        try {
            return Response.ok().setPayload(clasificadorRuatService.actualizarClasificador(clasificadorRuatDto));
        } catch (NotFoundException ex) {
            return Response.notFound().setPayload(ex.getMessage());
        } catch (RuntimeException ex) {
            // Handle duplicate code and other runtime exceptions
            if (ex.getMessage().contains("Ya existe un clasificador con el código")) {
                return Response.duplicateEntity().setPayload(ex.getMessage());
            }
            return Response.unprocessableEntity().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.unprocessableEntity().setPayload("Error al actualizar el clasificador: " + ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Clasificador")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('/admin/classifiers','ANULAR')")
    public Response deleteCategory(@Parameter(description = "Uuid para eliminar categoria")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(clasificadorRuatService.eliminarClasificador(uuid));
    }
}
