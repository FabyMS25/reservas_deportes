package com.gamq.licencias.controller;


import com.gamq.licencias.dto.CategoriaDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    @Autowired
    private CategoriaService categoriaService;

    @GetMapping()
    public Response obtenerCategorias() {
        try{
            List<CategoriaDto> CategoriaDtos =  categoriaService.obtenerCategorias();
            return Response.ok().setPayload(CategoriaDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            CategoriaDto CategoriaDto = categoriaService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(CategoriaDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearCategoria(@RequestBody CategoriaDto CategoriaDto) {
        try{
            return Response.ok().setPayload(categoriaService.crearCategoria(CategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarCategoria(@RequestBody CategoriaDto CategoriaDto) {
        try{
            return Response.ok().setPayload(categoriaService.actualizarCategoria(CategoriaDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
