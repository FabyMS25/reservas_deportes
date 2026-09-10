package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Resolves upstream permission identity to resource routes. Display names are never security keys. */
@Component
public class PermissionCatalog {
    private final Map<String, String> resources = new HashMap<>();

    public PermissionCatalog(ObjectMapper mapper) throws IOException {
        try (InputStream in = new ClassPathResource("security/permission-resources.json").getInputStream()) {
            mapper.readTree(in).fields().forEachRemaining(e -> resources.put(e.getKey(), e.getValue().asText()));
        }
    }

    public static String key(String route, String action) {
        if (route == null || !route.startsWith("/") || action == null || action.isBlank()) return "";
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(MessageDigest.getInstance("SHA-256")
                    .digest((route + "|" + action).getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
    }

    public Set<String> fromMenu(JsonNode menu, String code, String application) {
        Set<String> result = new TreeSet<>();
        collect(menu, code, application, result);
        return Collections.unmodifiableSet(result);
    }

    private void collect(JsonNode node, String code, String application, Set<String> result) {
        if (node == null) return;
        if (node.isArray()) { node.forEach(child -> collect(child, code, application, result)); return; }
        if (!node.isObject()) return;
        ((ObjectNode) node).remove("permissionRoute");
        if (application.equals(node.path("modulo").asText()) && code.equals(node.path("codigo").asText())) {
            String action = node.path("accion").asText();
            String route = resources.get(node.path("uuid").asText() + "|" + action);
            // Unknown upstream identities cannot grant backend access.
            if (route != null && !action.isBlank()) {
                ((ObjectNode) node).put("permissionRoute", route);
                result.add(key(route, action));
            }
            collect(node.path("permisoHijoListDto"), code, application, result);
        }
    }
}
