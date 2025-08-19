package com.gamq.deportes.controller;

import com.gamq.deportes.dto.contribuyenteDto;
//import com.gamq.licencias.dto.contribuyenteDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.service.contribuyenteService;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/contribuyente")
public class contribuyenteController {
    @Autowired
    contribuyenteService contribuyenteService;

    @GetMapping()
    public Response obtenerContribuyentes() {
        try{
            List<contribuyenteDto> contribuyenteDtos =  contribuyenteService.obtenerContribuyentes();
            return Response.ok().setPayload(contribuyenteDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }

    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidcontribuyente) {
        try{
            contribuyenteDto contribuyenteDto = contribuyenteService.findByUuid(uuidcontribuyente);
            return Response.ok().setPayload(contribuyenteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @GetMapping("/email/{correo}")
    public Response buscarContribuyentePorEmail( @PathVariable("correo") String correo) {
        try{
            contribuyenteDto contribuyenteDto = contribuyenteService.findByEmail(correo);
            return Response.ok().setPayload(contribuyenteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @PostMapping()
    public Response crearConstribuyente(@RequestBody contribuyenteDto contribuyenteDto) {
        try{
            return Response.ok().setPayload(contribuyenteService.crearContribuyente(contribuyenteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @PutMapping()
    public Response actualizarConstruccion(@RequestBody contribuyenteDto contribuyenteDto) {
        try{
            return Response.ok().setPayload(contribuyenteService.actualizarContribuyente(contribuyenteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }


    @PostMapping("/login")
    public Response loginContribuyente(@RequestBody login login ) {
        try{
            return Response.ok().setPayload(contribuyenteService.loginContribuyente(login));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @DeleteMapping("/{uuid}")
    public Response deleteUser(@Parameter(description = "Uuid para eliminar contribuyente")
                                  @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(contribuyenteService.delete(uuid));
    }
}
