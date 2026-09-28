package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("api/ruat")
@Tag(name = "Api RUAT", description = "Servicios de RUAT")
public class ApiController {
    @Autowired
    private  ApiService apiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Verificar autenticación RUAT mediante ruat-be, sin devolver el token")
    @PostMapping("/login")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkRuatAvailability() {
        try {
            return ResponseEntity.ok(apiService.checkRuatAvailability());
        } catch (Exception ex) {
            return toRuatHttpError(ex);
        }
    }

    @Operation(summary = "Buscar un Contribuyente")
    @PostMapping("/buscar")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> buscar(@RequestBody ContribuyenteToken contribuyenteToken) {
        try{
            return this.apiService.buscarContribuyente(contribuyenteToken);
        } catch(Exception ex) {
            return toRuatHttpError(ex);
        }
    }
    @Operation(summary = "Crea una tasa en RUAT")
    @PostMapping("/creartasa")
    @PreAuthorize("@permissions.anyReservation('CREAR')")
    public ResponseEntity<?> crearTasa(@RequestBody TasaToken tasaToken) {

        try{

            return this.apiService.crearTasa(tasaToken);
        } catch(Exception ex) {
            return toRuatHttpError(ex);
        }
    }
    @Operation(summary = "Registro de Contribuyente")
    @PostMapping("/registro")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> registrar(@RequestBody RegistroToken registroToken) {

        try{

            return this.apiService.registro(registroToken);
        } catch(Exception ex) {
            return toRuatHttpError(ex);
        }
    }
    @Operation(summary = "VErifica pago de una tasa")
    @PostMapping("/pago")
    @PreAuthorize("#p0 != null and #p0.pago != null and @permissions.ruatRate('VER',#p0.pago.numeroTasa)")
    public ResponseEntity<?> consultaPago(@RequestBody PagoToken pagoToken) {

        try{

            return this.apiService.consultaPago(pagoToken);
        } catch(Exception ex) {
            return toRuatHttpError(ex);
        }
    }

    private ResponseEntity<?> toRuatHttpError(Exception ex) {
        if (ex instanceof HttpStatusCodeException) {
            HttpStatusCodeException upstream = (HttpStatusCodeException) ex;
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            if (upstream.getResponseHeaders() != null) {
                for (String name : new String[]{"Content-Type", "Content-Encoding", "X-Response-Origin", "X-Request-ID"}) {
                    if (upstream.getResponseHeaders().containsKey(name)) headers.put(name, upstream.getResponseHeaders().get(name));
                }
            }
            return ResponseEntity.status(upstream.getRawStatusCode()).headers(headers)
                    .body(upstream.getResponseBodyAsByteArray());
        }
        if (ex instanceof ResourceAccessException) {
            return localError(HttpStatus.BAD_GATEWAY,
                    "Deportes no pudo leer una respuesta HTTP completa de ruat-be. Consulte el registro con el identificador de solicitud.");
        }
        if (ex instanceof IllegalArgumentException) {
            return localError(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
        if (ex instanceof IllegalStateException) {
            return localError(HttpStatus.BAD_GATEWAY, ex.getMessage());
        }
        return localError(HttpStatus.BAD_GATEWAY, "No se pudo procesar la solicitud. Contacte al administrador.");
    }

    private ResponseEntity<?> localError(HttpStatus status, String message) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("mensaje", message == null || message.isBlank() ? "No se pudo procesar la solicitud." : message);
        body.put("continuarFlujo", false);
        ObjectNode detail = body.putObject("detalle").put("origen", "deportes-be");
        org.springframework.web.context.request.ServletRequestAttributes context =
                (org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
        if (context != null) {
            Object requestId = context.getRequest().getAttribute("ruat.requestId");
            if (requestId != null) detail.put("requestId", requestId.toString());
        }
        return ResponseEntity.status(status).header("X-Response-Origin", "deportes-be").body(body);
    }

}
