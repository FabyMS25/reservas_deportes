package com.gamq.licencias.controller;

import com.gamq.licencias.dto.CategoriaDto;
import com.gamq.licencias.dto.TarifaCategoriaDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.CategoriaService;
import com.gamq.licencias.service.TarifaCategoriaService;
import com.gamq.licencias.service.TarifasService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/tarifacategoria")
public class TarifasCategoriaController {
    @Autowired
    TarifaCategoriaService tarifaCategoriaService;
    
    @Autowired
    TarifasService tarifasService;
    
    @Autowired
    CategoriaService categoriaService;
    
    
    @GetMapping()
    public Response obtenerTarifaCategorias() {
        try{
            List<TarifaCategoriaDto> TarifaCategoriaDtos =  tarifaCategoriaService.obtenerTarifaCategorias();
            return Response.ok().setPayload(TarifaCategoriaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            TarifaCategoriaDto TarifaCategoriaDto = tarifaCategoriaService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(TarifaCategoriaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearCategoria(@RequestBody TarifaCategoriaDto TarifaCategoriaDto) {
        try{
            return Response.ok().setPayload(tarifaCategoriaService.crearTarifaCategoria(TarifaCategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarCategoria(@RequestBody TarifaCategoriaDto TarifaCategoriaDto) {
        try{
            return Response.ok().setPayload(tarifaCategoriaService.actualizarTarifaCategoria(TarifaCategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @DeleteMapping("/{uuid}")
    public Response deleteTarifaCategory(@Parameter(description = "Uuid para eliminar relacion tarida categoria")
                                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(tarifaCategoriaService.deleteTarifaCategory(uuid));
    }
}
