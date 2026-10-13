package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Builds the signed permission snapshot from the menu data returned by Usuarios. */
@Component
public class PermissionCatalog {
    public static String key(String route, String action) {
        if (!validRoute(route) || action == null || action.isBlank()) {
            return "";
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((route + "|" + action).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Set<String> fromMenu(JsonNode menu, String code, String application) {
        Set<String> permissions = new TreeSet<>();
        collect(menu, code, application, permissions);
        return Collections.unmodifiableSet(permissions);
    }

    private void collect(JsonNode node, String code, String application, Set<String> permissions) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collect(child, code, application, permissions));
            return;
        }
        if (!node.isObject()) {
            return;
        }

        ObjectNode menuItem = (ObjectNode) node;
        menuItem.remove("permissionRoute");
        menuItem.remove("uuid");

        if (!application.equals(menuItem.path("modulo").asText())
                || !code.equals(menuItem.path("codigo").asText())) {
            return;
        }

        String route = menuItem.path("ruta").asText();
        String action = menuItem.path("accion").asText();
        if (validRoute(route) && !action.isBlank()) {
            menuItem.put("permissionRoute", route);
            permissions.add(key(route, action));
        }

        collect(menuItem.path("permisoHijoListDto"), code, application, permissions);
    }

    private static boolean validRoute(String route) {
        return route != null && route.startsWith("/") && !route.startsWith("//");
    }
}