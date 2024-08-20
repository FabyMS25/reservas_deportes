package com.gamq.licencias.controller;


import com.gamq.licencias.dto.encargadoComplejoDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.service.encargadoComplejoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/encargado")
public class encargadoComplejoController {

    @Autowired
    encargadoComplejoService encargadoCcomplejoService;

    @GetMapping()
    public Response obtenerEncargados() {
        try {
            List<encargadoComplejoDto> encargadoComplejoDtos = encargadoCcomplejoService.obtenerEncargadosComplejos();
            return Response.ok().setPayload(encargadoComplejoDtos);
        } catch (Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @GetMapping("/{uuid}")
    public Response buscarEncargadoPorUuid( @PathVariable("uuid") String uuid) {
        try{
            encargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUuid(uuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }

    @GetMapping("user/{userUuid}")
    public Response buscarEncargadoPorUserUuid( @PathVariable("userUuid") String userUuid) {
        try{
            encargadoComplejoDto encargadoComplejoDto = encargadoCcomplejoService.findByUserUuid(userUuid);
            return Response.ok().setPayload(encargadoComplejoDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping()
    public Response crearEncargado(@RequestBody encargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.crearEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarEncargado(@RequestBody encargadoComplejoDto encargadoComplejoDto) {
        try{
            return Response.ok().setPayload(encargadoCcomplejoService.actualizarEncargadoComplejo(encargadoComplejoDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
}
