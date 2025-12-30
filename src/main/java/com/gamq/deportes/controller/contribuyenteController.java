package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ContribuyenteDto;
//import com.gamq.licencias.dto.ContribuyenteDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.service.ContribuyenteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/contribuyente")
@Tag(name = "Contribuyentes", description = "Contribuyentes que toman Servicios en GAMQ ")
public class contribuyenteController {
    @Autowired
    ContribuyenteService contribuyenteService;
    @Operation(summary = "Listar Contribuyentes")
    @GetMapping()
    public Response obtenerContribuyentes() {
        try{
            List<ContribuyenteDto> contribuyenteDtos =  contribuyenteService.obtenerContribuyentes();
            return Response.ok().setPayload(contribuyenteDtos);
        } catch(Exception ex) {
            return Response.notContent().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar contribuyente por uuid")
    @GetMapping("/{uuid}")
    public Response buscarConstruccionPorUuid( @PathVariable("uuid") String uuidcontribuyente) {
        try{
            ContribuyenteDto contribuyenteDto = contribuyenteService.findByUuid(uuidcontribuyente);
            return Response.ok().setPayload(contribuyenteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Buscar contribuyente por email")
    @GetMapping("/email/{correo}")
    public Response buscarContribuyentePorEmail( @PathVariable("correo") String correo) {
        try{
            ContribuyenteDto contribuyenteDto = contribuyenteService.findByEmail(correo);
            return Response.ok().setPayload(contribuyenteDto);
        } catch(Exception ex) {
            return Response.notFound().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Crear contribuyente")
    @PostMapping()
    public Response crearConstribuyente(@RequestBody ContribuyenteDto contribuyenteDto) {
        try{
            return Response.ok().setPayload(contribuyenteService.crearContribuyente(contribuyenteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Actuañlizar contribuyente ")
    @PutMapping()
    public Response actualizarConstruccion(@RequestBody ContribuyenteDto contribuyenteDto) {
        try{
            return Response.ok().setPayload(contribuyenteService.actualizarContribuyente(contribuyenteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Login a RUAT contribuyente")
    @PostMapping("/login")
    public Response loginContribuyente(@RequestBody login login ) {
        try{
            return Response.ok().setPayload(contribuyenteService.loginContribuyente(login));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }
    @Operation(summary = "Borrar contribuyente por uuid")
    @DeleteMapping("/{uuid}")
    public Response deleteUser(@Parameter(description = "Uuid para eliminar contribuyente")
                                  @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(contribuyenteService.delete(uuid));
    }
}
