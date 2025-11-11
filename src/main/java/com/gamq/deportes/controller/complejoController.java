package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ComplejoDeportivoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ComplejoService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complejos")
public class complejoController {
    @Autowired
    private ComplejoService complejoService;

    @GetMapping()
    public Response obtenercomplejos() {
        try{
            List<ComplejoDeportivoDto> complejoDtos =  complejoService.obtenerComplejos();
            return Response.ok().setPayload(complejoDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarcomplejoPorUuid( @PathVariable("uuid") String uuidcomplejo) {
        try{
            ComplejoDeportivoDto complejoDto = complejoService.findByUuid(uuidcomplejo);
            return Response.ok().setPayload(complejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @PostMapping()
    public Response crearcomplejo(@RequestBody ComplejoDeportivoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.crearComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarcomplejo(@RequestBody ComplejoDeportivoDto complejoDto) {
        try{
            return Response.ok().setPayload(complejoService.actualizarComplejo(complejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteComplex(@Parameter(description = "Uuid para eliminar complejo")
                                @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(complejoService.eliminarComplejo(uuid));
    }
}
