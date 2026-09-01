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
import com.gamq.deportes.model.login.LoginResponse;
import com.gamq.deportes.service.ApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
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
    public Response login() {
        try {
            LoginResponse loginResponse = this.apiService.loginWithConfiguredCredentials();
            boolean available = loginResponse != null
                    && loginResponse.getToken() != null
                    && !loginResponse.getToken().trim().isEmpty();
            return Response.ok().setPayload(objectMapper.createObjectNode().put("available", available));
        } catch (Exception ex) {
            return toRuatErrorResponse(ex);
        }
    }
    @Operation(summary = "Buscar un Contribuyente")
    @PostMapping("/buscar")
    public ResponseEntity<Object> buscar(@RequestBody ContribuyenteToken contribuyenteToken) {
        try{
            JsonNode contribuyenteResponse=this.apiService.buscarContribuyente(contribuyenteToken);
            return ResponseEntity.ok(contribuyenteResponse);
        } catch(Exception ex) {
            return toRuatHttpError(ex, contribuyenteToken != null ? contribuyenteToken.getContribuyenteRequest() : null);
        }
    }
    @Operation(summary = "Crea una tasa en RUAT")
    @PostMapping("/creartasa")
    public ResponseEntity<Object> crearTasa(@RequestBody TasaToken tasaToken) {

        try{

            JsonNode tasaResponse=this.apiService.crearTasa(tasaToken);
            return ResponseEntity.ok(tasaResponse);
        } catch(Exception ex) {
            return toRuatHttpError(ex, tasaToken != null ? tasaToken.getTasa() : null);
        }
    }
    @Operation(summary = "Anular Tasa")
    @PostMapping("/anulartasa")
    public ResponseEntity<Object> anularTasa(@RequestBody AnularTasaToken anularTasaToken) {
        try{
            JsonNode anularTasaResponse= this.apiService.anularTasa(anularTasaToken);
            return ResponseEntity.ok(anularTasaResponse);
        } catch(Exception ex) {
            return toRuatHttpError(ex, anularTasaToken != null ? anularTasaToken.getAnular() : null);
        }
    }
    @Operation(summary = "Registro de Contribuyente")
    @PostMapping("/registro")
    public ResponseEntity<Object> registrar(@RequestBody RegistroToken registroToken) {

        try{

            JsonNode registroResponse= this.apiService.registro(registroToken);
            return ResponseEntity.ok(registroResponse);
        } catch(Exception ex) {
            return toRuatHttpError(ex, registroToken != null ? registroToken.getRegistro() : null);
        }
    }
    @Operation(summary = "VErifica pago de una tasa")
    @PostMapping("/pago")
    public ResponseEntity<Object> consultaPago(@RequestBody PagoToken pagoToken) {

        try{

            JsonNode pagoResponse= this.apiService.consultaPago(pagoToken);
            return ResponseEntity.ok(pagoResponse);
        } catch(Exception ex) {
            return toRuatHttpError(ex, pagoToken != null ? pagoToken.getPago() : null);
        }
    }

    @GetMapping()
    public String cadena(){
        return "prueba";
    }

    private Response toRuatErrorResponse(Exception ex) {
        return toRuatErrorResponse(ex, null);
    }

    private ResponseEntity<Object> toRuatHttpError(Exception ex, Object ruatRequest) {
        Response<?> error = toRuatErrorResponse(ex, ruatRequest);
        HttpStatus status;
        switch (error.getStatus()) {
            case NOT_FOUND:
                status = HttpStatus.NOT_FOUND;
                break;
            case BAD_REQUEST:
            case VALIDATION_EXCEPTION:
                status = HttpStatus.BAD_REQUEST;
                break;
            default:
                status = HttpStatus.BAD_GATEWAY;
        }
        return ResponseEntity.status(status).body(error.getPayload());
    }

    private Response toRuatErrorResponse(Exception ex, Object ruatRequest) {
        RuatApiResponseDto payload = new RuatApiResponseDto()
                .setMensaje("No se pudo procesar la solicitud en RUAT")
                .setContinuarFlujo(false);

        if (ex instanceof HttpStatusCodeException) {
            HttpStatusCodeException httpException = (HttpStatusCodeException) ex;
            JsonNode originalRuatBody = parseJson(httpException.getResponseBodyAsString());
            if (originalRuatBody != null) {
                if (httpException.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return Response.notFound().setPayload(originalRuatBody);
                }
                if (httpException.getStatusCode().is4xxClientError()) {
                    return Response.badRequest().setPayload(originalRuatBody);
                }
                return Response.exception().setPayload(originalRuatBody);
            }
            payload.setMensaje(cleanExceptionMessage(httpException.getResponseBodyAsString()));
            return httpException.getStatusCode().is4xxClientError()
                    ? Response.badRequest().setPayload(payload)
                    : Response.exception().setPayload(payload);
        }

        if (ex instanceof ResourceAccessException) {
            payload.setMensaje("No se pudo conectar con RUAT. Intente nuevamente.");
            payload.setDetalle(ex.getMessage());
            return Response.exception().setPayload(payload);
        }

        if (ex instanceof IllegalArgumentException) {
            payload.setMensaje(cleanExceptionMessage(ex.getMessage()));
            return Response.badRequest().setPayload(payload);
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

    private JsonNode parseJson(String body) {
        if (body == null || body.trim().isEmpty()) return null;
        try {
            return objectMapper.readTree(body);
        } catch (Exception ignored) {
            return null;
        }
    }

    private ObjectNode createErrorDetail(HttpStatusCodeException ex, Object ruatRequest) {
        ObjectNode detail = objectMapper.createObjectNode();
        detail.put("httpStatus", ex.getRawStatusCode());

        String body = ex.getResponseBodyAsString();
        if (body != null && !body.trim().isEmpty()) {
            try {
                JsonNode ruatBody = objectMapper.readTree(body);
                detail.set("respuestaRuat", ruatBody);
                JsonNode additionalData = removePromotedFields(ruatBody);
                if (additionalData != null && !additionalData.isEmpty()) {
                    detail.set("datosRuat", additionalData);
                }
                ObjectNode validationDetail = validationDetail(ruatBody.get("mensaje"), ruatRequest);
                if (validationDetail != null) {
                    detail.set("validacion", validationDetail);
                }
            } catch (Exception ignored) {
                // A plain-text RUAT response is already exposed as "mensaje".
            }
        }
        return detail;
    }

    private ObjectNode validationDetail(JsonNode mensajeNode, Object ruatRequest) {
        if (mensajeNode == null || mensajeNode.isNull()) {
            return null;
        }
        ObjectNode requestNode = ruatRequest == null
                ? objectMapper.createObjectNode()
                : objectMapper.convertValue(ruatRequest, ObjectNode.class);
        return validationDetailFromNode(mensajeNode, requestNode);
    }

    private ObjectNode validationDetailFromNode(JsonNode node, ObjectNode requestNode) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                ObjectNode detail = validationDetailFromNode(child, requestNode);
                if (detail != null) {
                    return detail;
                }
            }
            return null;
        }
        if (!node.isObject()) {
            return null;
        }
        java.util.Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldMessages = node.get(fieldName);
            String message = firstTextValue(fieldMessages);
            if (message != null && !message.trim().isEmpty()) {
                ObjectNode detail = objectMapper.createObjectNode();
                detail.put("campo", fieldName);
                detail.put("mensaje", HtmlUtils.htmlUnescape(message).trim());
                JsonNode sentValue = requestNode.get(fieldName);
                if (sentValue != null && !sentValue.isNull()) {
                    detail.set("valorEnviado", sentValue);
                }
                return detail;
            }
        }
        return null;
    }

    private String validationMessage(ObjectNode detail) {
        JsonNode validation = detail.get("validacion");
        if (validation == null || validation.isNull()) {
            return null;
        }
        String field = textValue(validation, "campo", null);
        String message = textValue(validation, "mensaje", null);
        if (field == null || message == null) {
            return null;
        }
        JsonNode sentValue = validation.get("valorEnviado");
        if (sentValue != null && !sentValue.isNull()) {
            return field + " (valor enviado: " + sentValue.asText() + "): " + message;
        }
        return field + ": " + message;
    }

    private JsonNode removePromotedFields(JsonNode ruatBody) {
        if (ruatBody == null || ruatBody.isNull()) {
            return null;
        }
        if (!ruatBody.isObject()) {
            return ruatBody;
        }

        ObjectNode additionalData = ((ObjectNode) ruatBody).deepCopy();
        additionalData.remove("mensaje");
        additionalData.remove("continuarFlujo");
        return additionalData;
    }

    private String textValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return defaultValue;
        }
        String value = firstTextValue(field);
        return value == null || value.trim().isEmpty()
                ? defaultValue
                : HtmlUtils.htmlUnescape(value).trim();
    }

    private String firstTextValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isValueNode()) {
            return node.asText();
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                String value = firstTextValue(child);
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            }
            return null;
        }
        if (node.isObject()) {
            JsonNode observacion = node.get("observacion");
            String value = firstTextValue(observacion);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
            java.util.Iterator<JsonNode> children = node.elements();
            while (children.hasNext()) {
                value = firstTextValue(children.next());
                if (value != null && !value.trim().isEmpty()) {
                    return value;
                }
            }
        }
        return null;
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
        return HtmlUtils.htmlUnescape(message)
                .replaceAll("^\\d{3}\\s[^:]*:\\s*", "")
                .replaceAll("^\\[|\\]$", "")
                .trim();
    }
}
