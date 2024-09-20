package com.gamq.deportes.controller;

import com.gamq.deportes.dto.SuperficieDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.SuperficieService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/superficie")
public class SuperficieController {
    @Autowired
    SuperficieService superficieService;
    
    @GetMapping()
    public Response obtenerCategorias() {
        try{
            List<SuperficieDto> SuperficieDtos =  superficieService.obtenerSuperficies();
            return Response.ok().setPayload(SuperficieDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarCategoriaPorUuid( @PathVariable("uuid") String uuidCategoria) {
        try{
            SuperficieDto SuperficieDto = superficieService.findByUuid(uuidCategoria);
            return Response.ok().setPayload(SuperficieDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearCategoria(@RequestBody SuperficieDto SuperficieDto) {
        try{
            return Response.ok().setPayload(superficieService.crearSuperficie(SuperficieDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarCategoria(@RequestBody SuperficieDto SuperficieDto) {
        try{
            return Response.ok().setPayload(superficieService.actualizarSuperficie(SuperficieDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteSurface(@Parameter(description = "Uuid para eliminar superficie")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(superficieService.delete(uuid));
    }
}
