package com.gamq.deportes.controller;

import com.gamq.deportes.dto.ContribuyenteDto;
//import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.ContribuyenteService;
import com.gamq.deportes.exception.DuplicateTaxpayerException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/contribuyente")
@Tag(name = "Contribuyentes", description = "Contribuyentes que toman Servicios en GAMQ ")
public class TaxpayerController {
    @Autowired
    ContribuyenteService contribuyenteService;

    @GetMapping("/public/exists")
    public Response<Map<String, Boolean>> publicExistenceCheck(
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String userName) {
        Map<String, Boolean> result = new LinkedHashMap<>();
        if (numeroDocumento != null) {
            result.put("numeroDocumento", contribuyenteService.existsByNumeroDocumento(numeroDocumento));
        }
        if (email != null) {
            result.put("email", contribuyenteService.existsByEmail(email));
        }
        if (userName != null) {
            result.put("userName", contribuyenteService.existsByUserName(userName));
        }
        return Response.<Map<String, Boolean>>ok().setPayload(result);
    }
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
        } catch (DuplicateTaxpayerException ex) {
            return Response.duplicateEntity().setPayload(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            return Response.badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.unprocessableEntity()
                    .setPayload("No se pudo registrar al contribuyente. Verifique los datos e intente nuevamente.");
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

    @Operation(summary = "Borrar contribuyente por uuid")
    @DeleteMapping("/{uuid}")
    public Response deleteUser(@Parameter(description = "Uuid para eliminar contribuyente")
                                  @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(contribuyenteService.delete(uuid));
    }
}
