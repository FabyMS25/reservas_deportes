package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

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


@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/contribuyente")
@Tag(name = "Contribuyentes", description = "Contribuyentes que toman Servicios en GAMQ ")
public class TaxpayerController {
    @Autowired
    ContribuyenteService contribuyenteService;

    @GetMapping("/public/exists")
    @PreAuthorize("permitAll()")
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
    @PreAuthorize("@permissions.has('/taxpayers','VER') or (@permissions.staff() and @permissions.anyReservation('CREAR'))")
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
    @PreAuthorize("@permissions.self(#p0) or @permissions.has('/taxpayers','VER') or (@permissions.staff() and @permissions.anyReservation('CREAR'))")
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
    @PreAuthorize("@permissions.has('/taxpayers','VER')")
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
    @PreAuthorize("permitAll()")
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
    @PreAuthorize("#p0 != null and (@permissions.self(#p0.uuid) or @permissions.has('/taxpayers','ACTUALIZAR') or @permissions.has('/taxpayers','BLOQUEAR'))")
    public Response actualizarConstruccion(@RequestBody ContribuyenteDto contribuyenteDto) {
        try{
            return Response.ok().setPayload(contribuyenteService.actualizarContribuyente(contribuyenteDto));
        } catch (Exception ex) {
            return  Response.unprocessableEntity().setPayload(ex.getMessage());
        }
    }

    @Operation(summary = "Cambiar contraseña del contribuyente autenticado")
    @PostMapping("/change-password")
    @PreAuthorize("#p0 != null and @permissions.self(#p0.uuid)")
    public Response<String> cambiarContrasena(@RequestBody ContribuyenteDto request) {
        try {
            if (request == null) throw new IllegalArgumentException("Solicitud inválida");
            contribuyenteService.changePassword(request.getUuid(), request.getCurrentPassword(), request.getPassword());
            return Response.<String>ok().setPayload("La contraseña fue actualizada correctamente.");
        } catch (IllegalArgumentException ex) {
            return Response.<String>badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.<String>unprocessableEntity().setPayload("No se pudo actualizar la contraseña.");
        }
    }

    @Operation(summary = "Borrar contribuyente por uuid")
    @DeleteMapping("/{uuid}")
    @PreAuthorize("@permissions.has('/taxpayers','ANULAR')")
    public Response deleteUser(@Parameter(description = "Uuid para eliminar contribuyente")
                                  @PathVariable("uuid") String uuid) {
        return Response.ok().setPayload(contribuyenteService.delete(uuid));
    }
}
