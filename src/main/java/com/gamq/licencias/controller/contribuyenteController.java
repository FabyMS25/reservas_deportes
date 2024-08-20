package com.gamq.licencias.controller;

import com.gamq.licencias.dto.contribuyenteDto;
//import com.gamq.licencias.dto.contribuyenteDto;
import com.gamq.licencias.dto.response.Response;
import com.gamq.licencias.model.login.login;
import com.gamq.licencias.service.contribuyenteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.HashMap;
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

}
