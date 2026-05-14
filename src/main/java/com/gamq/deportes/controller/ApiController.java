package com.gamq.deportes.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gamq.deportes.dto.RuatApiResponseDto;
import com.gamq.deportes.model.AnularTasa.AnularTasaResponse;
import com.gamq.deportes.model.AnularTasa.AnularTasaToken;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteResponse;
import com.gamq.deportes.model.Contribuyentes.ContribuyenteToken;
import com.gamq.deportes.model.Pagos.PagoResponse;
import com.gamq.deportes.model.Pagos.PagoToken;
import com.gamq.deportes.model.Registro.RegistroResponse;
import com.gamq.deportes.model.Registro.RegistroToken;
import com.gamq.deportes.model.Tasa.TasaResponse;
import com.gamq.deportes.model.Tasa.TasaToken;
import com.gamq.deportes.model.login.LoginRequest;
import com.gamq.deportes.model.login.LoginResponse;
import com.gamq.deportes.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.bind.annotation.*;
import com.gamq.deportes.dto.response.Response;

@RestController
@RequestMapping("api/ruat")
@Tag(name = "Api RUAT", description = "Servicios de RUAT")
public class ApiController {
    @Autowired
    private  ApiService apiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Login a Ruat se obtiene un Token")
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return this.apiService.login(loginRequest);
    }
    @Operation(summary = "Buscar un Contribuyente")
    @PostMapping("/buscar")
    public Response Buscar(@RequestBody ContribuyenteToken contribuyenteToken) {
        try{
            ContribuyenteResponse contribuyenteResponse=this.apiService.buscarContribuyente(contribuyenteToken);
            return Response.ok().setPayload(toRuatApiResponse(contribuyenteResponse));
        } catch(Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }
    @Operation(summary = "Crea una tasa en RUAT")
    @PostMapping("/creartasa")
    public Response Anular(@RequestBody TasaToken tasaToken) {

        try{

            TasaResponse tasaResponse=this.apiService.crearTasa(tasaToken);
            return Response.ok().setPayload(toRuatApiResponse(tasaResponse));
        } catch(Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }
    @Operation(summary = "Anular Tasa")
    @PostMapping("/anulartasa")
    public Response Anular(@RequestBody AnularTasaToken anularTasaToken) {
        try{
            AnularTasaResponse anularTasaResponse= this.apiService.anularTasa(anularTasaToken);
            return Response.ok().setPayload(toRuatApiResponse(anularTasaResponse));
        } catch(Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }
    @Operation(summary = "Registro de Contribuyente")
    @PostMapping("/registro")
    public Response Registrar(@RequestBody RegistroToken registroToken) {

        try{

            RegistroResponse registroResponse= this.apiService.registro(registroToken);
            return Response.ok().setPayload(toRuatApiResponse(registroResponse));
        } catch(Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }
    @Operation(summary = "VErifica pago de una tasa")
    @PostMapping("/pago")
    public Response consultaPago(@RequestBody PagoToken pagoToken) {

        try{

            PagoResponse pagoResponse= this.apiService.consultaPago(pagoToken);
            return Response.ok().setPayload(toRuatApiResponse(pagoResponse));
        } catch(Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }

    @GetMapping()
    public String cadena(){
        return "prueba";
    }

    private RuatApiResponseDto toRuatApiResponse(Object ruatResponse) {
        ObjectNode responseNode = objectMapper.convertValue(ruatResponse, ObjectNode.class);
        String mensaje = textValue(responseNode, "mensaje", "Operacion procesada por RUAT");
        Boolean continuarFlujo = booleanValue(responseNode, "continuarFlujo");

        responseNode.remove("mensaje");
        responseNode.remove("continuarFlujo");

        return new RuatApiResponseDto()
                .setMensaje(mensaje)
                .setContinuarFlujo(continuarFlujo)
                .setDatos(responseNode);
    }

    private Response toRuatErrorResponse(Exception ex) {
        RuatApiResponseDto payload = new RuatApiResponseDto()
                .setMensaje("No se pudo procesar la solicitud en RUAT")
                .setContinuarFlujo(false);

        if (ex instanceof HttpStatusCodeException) {
            HttpStatusCodeException httpException = (HttpStatusCodeException) ex;
            payload = parseRuatErrorBody(httpException.getResponseBodyAsString(), payload)
                    .setDetalle(createErrorDetail(httpException));

            if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Response.notFound().setPayload(payload);
            }
            if (httpException.getStatusCode().is4xxClientError()) {
                return Response.badRequest().setPayload(payload);
            }
            return Response.exception().setPayload(payload);
        }

        if (ex instanceof ResourceAccessException) {
            payload.setMensaje("No se pudo conectar con RUAT. Intente nuevamente.");
            payload.setDetalle(ex.getMessage());
            return Response.exception().setPayload(payload);
        }

        payload.setMensaje(cleanExceptionMessage(ex.getMessage()));
        return Response.exception().setPayload(payload);
    }

    private RuatApiResponseDto parseRuatErrorBody(String body, RuatApiResponseDto fallback) {
        if (body == null || body.trim().isEmpty()) {
            return fallback;
        }

        try {
            JsonNode errorNode = objectMapper.readTree(body);
            String mensaje = textValue(errorNode, "mensaje", fallback.getMensaje());
            Boolean continuarFlujo = booleanValue(errorNode, "continuarFlujo");

            fallback.setMensaje(mensaje);
            if (continuarFlujo != null) {
                fallback.setContinuarFlujo(continuarFlujo);
            }
            return fallback;
        } catch (Exception ignored) {
            return fallback.setMensaje(cleanExceptionMessage(body));
        }
    }

    private ObjectNode createErrorDetail(HttpStatusCodeException ex) {
        ObjectNode detail = objectMapper.createObjectNode();
        detail.put("httpStatus", ex.getRawStatusCode());
        detail.put("statusText", ex.getStatusText());

        String body = ex.getResponseBodyAsString();
        if (body != null && !body.trim().isEmpty()) {
            try {
                detail.set("respuestaRuat", objectMapper.readTree(body));
            } catch (Exception ignored) {
                detail.put("respuestaRuat", cleanExceptionMessage(body));
            }
        }
        return detail;
    }

    private String textValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return defaultValue;
        }
        return field.asText(defaultValue);
    }

    private Boolean booleanValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isBoolean()) {
            return field.asBoolean();
        }
        String value = field.asText();
        if ("true".equalsIgnoreCase(value) || "1".equals(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value) || "0".equals(value)) {
            return false;
        }
        return null;
    }

    private String cleanExceptionMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "No se pudo procesar la solicitud en RUAT";
        }
        return message.replaceAll("^\\d{3}\\s[^:]*:\\s*", "")
                .replaceAll("^\\[|\\]$", "")
                .trim();
    }
}
