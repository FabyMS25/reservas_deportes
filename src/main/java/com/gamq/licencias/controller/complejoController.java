package com.gamq.licencias.controller;

import com.gamq.licencias.dto.complejoDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.complejoService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complejos")
public class complejoController {
    @Autowired
    private complejoService complejoService;

    @GetMapping()
    public Response obtenercomplejos() {
        try{
            List<complejoDto> complejoDtos =  complejoService.obtenerComplejos();
            return Response.ok().setPayload(complejoDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarcomplejoPorUuid( @PathVariable("uuid") String uuidcomplejo) {
        try{
            complejoDto complejoDto = complejoService.findByUuid(uuidcomplejo);
            return Response.ok().setPayload(complejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearcomplejo(@RequestBody complejoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.crearComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarcomplejo(@RequestBody complejoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.actualizarComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteComplex(@Parameter(description = "Uuid para eliminar complejo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(complejoService.deleteComplex(uuid));
    }
}
