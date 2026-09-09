package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.TarifaCategoriaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.CategoriaService;
import com.gamq.deportes.service.TarifaCategoriaService;
import com.gamq.deportes.service.TarifasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/tarifacategoria")
@Tag(name = "Tarifas Categorias", description = "API para gestion de tarifas de toma de servicios GAMQ")
public class TarifasCategoriaController {
    @Autowired
    TarifaCategoriaService tarifaCategoriaService;
    
    @Autowired
    TarifasService tarifasService;
    
    @Autowired
    CategoriaService categoriaService;

    @Operation(summary = "Obtener Tarifas Categorias")
    @GetMapping()
    @PreAuthorize("@permissions.lookup('Campos Deportivos')")
    public Response obtenerTarifaCategorias() {
        try{
            List<TarifaCategoriaDto> TarifaCategoriaDtos =  tarifaCategoriaService.obtenerTarifaCategorias();
            return Response.ok().setPayload(TarifaCategoriaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener Tarifa Categoria por uuid")
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.lookup('Campos Deportivos')")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            TarifaCategoriaDto TarifaCategoriaDto = tarifaCategoriaService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(TarifaCategoriaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Tarifa Categoria")
    @PostMapping()
    @PreAuthorize("@permissions.has('Campos Deportivos','CREAR')")
    public Response crearCategoria(@RequestBody TarifaCategoriaDto TarifaCategoriaDto) {
        try{
            return Response.ok().setPayload(tarifaCategoriaService.crearTarifaCategoria(TarifaCategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Tarifa Categoria")
    @PutMapping()
    @PreAuthorize("@permissions.has('Campos Deportivos','ACTUALIZAR')")
    public Response actualizarCategoria(@RequestBody TarifaCategoriaDto TarifaCategoriaDto) {
        try{
            return Response.ok().setPayload(tarifaCategoriaService.actualizarTarifaCategoria(TarifaCategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar Tarifa Categoria por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('Campos Deportivos','ANULAR')")
    public Response deleteTarifaCategory(@Parameter(description = "Uuid para eliminar relacion tarida categoria")
                                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(tarifaCategoriaService.delete(uuid));
    }
}
