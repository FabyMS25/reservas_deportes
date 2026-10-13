package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

public abstract class AbstractClient {
    private static final Logger log = LoggerFactory.getLogger(AbstractClient.class);
    @Value("${ruat-be.gateway.base-url}")
    private String gatewayUrl;
    @Value("${ruat-be.gateway.api-key}")
    private String internalApiKey;
    @Value("${app.name}")
    private String systemCode;
    private final RestTemplate restTemplate;

    protected AbstractClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected ResponseEntity<byte[]> gatewayPost(String path, Object payload) {
        return gatewayPost(path, payload, restTemplate);
    }

    protected ResponseEntity<byte[]> gatewayPost(String path, Object payload, RestTemplate client) {
        ServletRequestAttributes context = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String requestId = context == null ? null : (String) context.getRequest().getAttribute("ruat.requestId");
        if (requestId == null) requestId = UUID.randomUUID().toString();
        if (context != null) {
            context.getRequest().setAttribute("ruat.requestId", requestId);
            if (context.getResponse() != null) context.getResponse().setHeader("X-Request-ID", requestId);
        }
        if (internalApiKey == null || internalApiKey.trim().isEmpty()) {
            throw new IllegalStateException("Deportes no tiene configurada la clave de acceso a ruat-be (ruat-be.gateway.api-key). No se realizó la solicitud a RUAT.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("X-Api-Key", internalApiKey);
        headers.set("X-System-Code", systemCode);
        headers.set("X-Request-ID", requestId);
        long started = System.nanoTime();
        java.net.URI destination = java.net.URI.create(gatewayUrl);
        log.info("requestId={} stage=deportes-to-gateway inbound={} targetHost={} targetPort={} operation={} payloadType={} keyPresent=true",
                requestId, context == null ? "internal" : context.getRequest().getServletPath(),
                destination.getHost(), destination.getPort(), path, payload.getClass().getSimpleName());
        String target = gatewayUrl.replaceAll("/+$", "") + path;
        ResponseEntity<byte[]> response;
        try {
            response = client.exchange(target,
                    HttpMethod.POST, new HttpEntity<>(payload, headers), byte[].class);
            log.info("requestId={} stage=gateway-response status={} elapsedMs={}", requestId,
                    response.getStatusCodeValue(), (System.nanoTime() - started) / 1000000);
        } catch (RestClientResponseException ex) {
            log.warn("requestId={} stage=gateway-response status={} elapsedMs={}", requestId,
                    ex.getRawStatusCode(), (System.nanoTime() - started) / 1000000);
            return forwardResponse(target, ex.getRawStatusCode(), ex.getResponseHeaders(), ex.getResponseBodyAsByteArray());
        } catch (RestClientException ex) {
            log.warn("requestId={} stage=gateway-transport failure={} elapsedMs={}", requestId,
                    ex.getClass().getSimpleName(), (System.nanoTime() - started) / 1000000);
            throw ex;
        }
        return forwardResponse(target, response.getStatusCodeValue(), response.getHeaders(), response.getBody());
    }

    protected JsonNode gatewayAvailability() {
        ResponseEntity<byte[]> response = gatewayPost("/auth/check", Collections.emptyMap());
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new org.springframework.web.client.HttpClientErrorException(response.getStatusCode(),
                    "Gateway authentication check", response.getHeaders(), response.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.getBody());
        } catch (java.io.IOException | IllegalArgumentException ex) {
            throw new IllegalStateException("La comprobación de disponibilidad del gateway no devolvió JSON.", ex);
        }
    }

    private ResponseEntity<byte[]> forwardResponse(String target, int status, HttpHeaders upstream, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        if (upstream != null) {
            for (String name : new String[]{HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_ENCODING,
                    HttpHeaders.CONTENT_LANGUAGE, HttpHeaders.RETRY_AFTER, "X-Response-Origin", "X-Request-ID"}) {
                if (upstream.containsKey(name)) headers.put(name, upstream.get(name));
            }
        }
        return ResponseEntity.status(status).headers(headers).body(body);
    }
}
