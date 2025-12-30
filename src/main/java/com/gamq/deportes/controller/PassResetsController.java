package com.gamq.deportes.controller;


import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PassResetsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/passresets")
@Tag(name = "Password Resests", description = "API para s")
public class PassResetsController {
    
    @Autowired
    PassResetsService passResetsService;
    @Operation(summary = "Obtener todos los Passwords")
    @GetMapping()
    public Response obtenerPass() {
        try{
            List<PassDto> passDtos =  passResetsService.obtenerPassResets();
            return Response.ok().setPayload(passDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Passwords por uuid")
    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidCanchas) {
        try{
            PassDto PassDto = passResetsService.findByUuid(uuidCanchas);
            return Response.ok().setPayload(PassDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener validacion de token")
    @GetMapping("/validate-token/{token}")
    public Response buscarPorToken( @PathVariable("token") String token) {
        try{
            PassDto PassDto = passResetsService.findByToken(token);
            return Response.ok().setPayload(PassDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Obtener usuario pro token")
    @GetMapping("/validate-token-get-user/{token}")
    public Response buscarUserPorToken( @PathVariable("token") String token) {
        try{
            ContribuyenteDto contribuyenteDto = passResetsService.findUserByToken(token);
            return Response.ok().setPayload(contribuyenteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear Passwords")
    @PostMapping()
    public Response crearPass(@RequestBody PassDto PassDto) {
        try{
            return Response.ok().setPayload(passResetsService.crearPassResets(PassDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actualizar Passwords")
    @PutMapping()
    public Response actualizarPass(@RequestBody PassDto PassDto) {
        try{
            return Response.ok().setPayload(passResetsService.actualizarPassResets(PassDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

}
