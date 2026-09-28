package com.gamq.deportes.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.gamq.deportes.security.DeportesPrincipal;

import java.util.Optional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AuditorAwareimpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(currentActor());
    }

    public static String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal) {
            String subject = ((DeportesPrincipal) authentication.getPrincipal()).getSubject();
            if (subject != null && !subject.trim().isEmpty()) {
                return subject.trim();
            }
        }
        return "SYSTEM";
    }

    public static String remoteAddress() {
        ServletRequestAttributes context = requestContext();
        // Do not trust a client-supplied address or forwarded header.
        return context == null ? null : context.getRequest().getRemoteAddr();
    }

    public static String userAgent() {
        ServletRequestAttributes context = requestContext();
        String value = context == null ? null : context.getRequest().getHeader("User-Agent");
        return value == null ? null : value.substring(0, Math.min(value.length(), 1024));
    }

    public static String withContext(String data) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode result = mapper.createObjectNode();
        if (data != null && !data.isBlank()) {
            try {
                JsonNode parsed = mapper.readTree(data);
                if (parsed != null && parsed.isObject()) result.setAll((ObjectNode) parsed);
                else result.set("detalle", parsed);
            } catch (java.io.IOException ex) {
                result.put("detalle", data);
            }
        }
        ServletRequestAttributes context = requestContext();
        ObjectNode source = result.putObject("contextoAuditoria");
        source.put("usuarioUuid", currentActor());
        source.put("origen", context == null ? "PROCESO_INTERNO" : "SOLICITUD_HTTP");
        if (context != null) {
            source.put("ruta", context.getRequest().getRequestURI());
            source.put("metodo", context.getRequest().getMethod());
            Object requestId = context.getRequest().getAttribute("ruat.requestId");
            if (requestId != null) source.put("requestId", requestId.toString());
        }
        return result.toString();
    }

    private static ServletRequestAttributes requestContext() {
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes
                ? (ServletRequestAttributes) RequestContextHolder.getRequestAttributes() : null;
    }
}
