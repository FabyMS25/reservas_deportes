package com.gamq.deportes.controller;


import com.gamq.deportes.dto.CategoriaDeportivaDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Categorias deportivas del GAMQ ")
public class CategoriaController {
    @Autowired
    private CategoriaService categoriaService;
    @Operation(summary = "Listar las Categorias")
    @GetMapping()
    public Response obtenerCategorias() {
        try{
            List<CategoriaDeportivaDto> CategoriaDtos =  categoriaService.obtenerCategorias();
            return Response.ok().setPayload(CategoriaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Categoria por uuid")
    @GetMapping("/{uuid}")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            CategoriaDeportivaDto CategoriaDto = categoriaService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(CategoriaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Categoria")
    @PostMapping()
    public Response crearCategoria(@RequestBody CategoriaDeportivaDto CategoriaDto) {
        try{
            return Response.ok().setPayload(categoriaService.crearCategoria(CategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Categoria")
    @PutMapping()
    public Response actualizarCategoria(@RequestBody CategoriaDeportivaDto CategoriaDto) {
        try{
            return Response.ok().setPayload(categoriaService.actualizarCategoria(CategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Eliminar Categoria por uuid")
    @DeleteMapping("/{uuid}")
    public Response deleteCategory(@Parameter(description = "Uuid para eliminar categoria")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(categoriaService.delete(uuid));
    }

}
