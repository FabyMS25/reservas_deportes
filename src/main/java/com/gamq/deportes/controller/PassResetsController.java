package com.gamq.deportes.controller;


import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.PassDto;
import com.gamq.deportes.dto.response.Response;
import com.gamq.deportes.service.PassResetsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/passresets")
@Tag(name = "Password Resests", description = "API para s")
public class PassResetsController {
    @Autowired
    PassResetsService passResetsService;
    @Operation(summary = "Validar enlace de recuperación de contraseña")
    @PostMapping("/validate-token")
    @PreAuthorize("permitAll()")
    public Response<Boolean> validarToken(@RequestBody PassDto request) {
        String token = request == null ? null : request.getToken();
        return Response.<Boolean>ok().setPayload(passResetsService.isResetTokenValid(token));
    }

    @Operation(summary = "Solicitar recuperación de contraseña de contribuyente")
    @PostMapping()
    @PreAuthorize("permitAll()")
    public Response<PassDto> solicitarRecuperacion(@RequestBody PassDto request) {
        String email = request == null ? null : request.getEmail();
        return Response.<PassDto>ok().setPayload(passResetsService.requestPasswordReset(email));
    }

    @Operation(summary = "Completar recuperación de contraseña de contribuyente")
    @PostMapping("/complete")
    @PreAuthorize("permitAll()")
    public Response<String> completarRecuperacion(@RequestBody PassDto request) {
        try{
            if (request == null) throw new IllegalArgumentException("Solicitud inválida");
            passResetsService.completePasswordReset(request.getToken(), request.getPassword());
            return Response.<String>ok().setPayload("La contraseña fue restablecida correctamente.");
        } catch (IllegalArgumentException ex) {
            return Response.<String>badRequest().setPayload(ex.getMessage());
        } catch (Exception ex) {
            return Response.<String>unprocessableEntity().setPayload("No se pudo restablecer la contraseña.");
        }
    }
}
