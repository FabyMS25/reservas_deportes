package com.gamq.deportes.context;


import java.io.IOException;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.http.client.*;


/** Logs serialized business exchanges, never authentication endpoints or credentials. */
public class RuatHttpLoggingInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger("ruat.http");
    private static final ObjectMapper JSON = new ObjectMapper();
    private final String hop;

    public RuatHttpLoggingInterceptor(String hop) { this.hop = hop; }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
            ClientHttpRequestExecution execution) throws IOException {
        String path = request.getURI().getPath();
        boolean business = request.getHeaders().containsKey("X-System-Code")
                || path.startsWith("/RuatServiciosWebTasasOI/tasasOI/")
                || path.startsWith("/RuatServiciosWebContribuyentes/contribuyentes/");
        if (!business || !log.isInfoEnabled()) return execution.execute(request, body);
        String id = request.getHeaders().getFirst("X-Request-ID");
        if (id == null) id = MDC.get("requestId");
        try {
            log.info("requestId={} hop={} direction=request\nmethod={}\nurl={}\ncontentType={} bytes={}\nbody:\n{}\n[end request]",
                    id, hop, request.getMethod(), request.getURI(), request.getHeaders().getFirst("Content-Type"),
                    body.length, printable(body, request.getHeaders()));
        } catch (RuntimeException loggingFailure) {
            // Observability must not prevent the request from being sent.
        }
        // Do not inspect, consume, wrap or close the response stream here.
        return execution.execute(request, body);
    }

    public static void logResponse(String hop, String id, int status, HttpHeaders headers, byte[] body) {
        try {
            if (!log.isInfoEnabled()) return;
            HttpHeaders safeHeaders = headers == null ? new HttpHeaders() : headers;
            byte[] bytes = body == null ? new byte[0] : body;
            log.info("requestId={} hop={} direction=response\nstatus={} contentType={} bytes={}\nbody:\n{}\n[end response]",
                    id, hop, status, safeHeaders.getFirst("Content-Type"), bytes.length, printable(bytes, safeHeaders));
        } catch (RuntimeException loggingFailure) {
            // The complete response remains available even if formatting/logging fails.
        }
    }
    private static String printable(byte[] bytes, HttpHeaders headers) {
        Charset charset = StandardCharsets.UTF_8;
        try {
            MediaType type = headers.getContentType();
            if (type != null && type.getCharset() != null) charset = type.getCharset();
            String text = new String(bytes, charset);
            if (text.isEmpty()) return text;
            try {
                // Formatting is only for the console; received bytes remain unchanged.
                return JSON.writerWithDefaultPrettyPrinter().writeValueAsString(JSON.readTree(text));
            } catch (Exception notJson) {
                return text;
            }
        } catch (Exception ex) {
            return "base64:" + java.util.Base64.getEncoder().encodeToString(bytes);
        }
    }
}
