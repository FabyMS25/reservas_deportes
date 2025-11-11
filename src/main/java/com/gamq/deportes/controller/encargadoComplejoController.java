package com.gamq.deportes.controller;


import com.gamq.deportes.dto.EncargadoComplejoDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.EncargadoComplejoService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encargado")
public class encargadoComplejoController {

    @Autowired
    EncargadoComplejoService encargadoCcomplejoService;

    @GetMapping()
    public Response obtenerEncargados() {
        try {
            List<EncargadoComplejoDto> encargadoComplejoDtos = encargadoCcomplejoService.obtenerEncargadosComplejos();
            return Response.ok().setPayload(encargadoComplejoDtos);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @GetMapping("/{uuid}")
    public Response buscarEncargadoPorUuid( @PathVariable("uuid") String uuid) {
        try{
            EncargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUuid(uuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("user/{userUuid}")
    public Response buscarEncargadoPorUserUuid( @PathVariable("userUuid") String userUuid) {
        try{
            EncargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUserUuid(userUuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping()
    public Response crearEncargado(@RequestBody EncargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.crearEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarEncargado(@RequestBody EncargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.actualizarEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @DeleteMapping("/{uuid}")
    public Response deleteEmployee(@Parameter(description = "Uuid para eliminar encargado")
                                 @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(encargadoCcomplejoService.delete(uuid));
    }
}
