package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
        Map<String, Set<String>> viewRoutes = new HashMap<>();
        collectViewRoutes(menu, code, application, viewRoutes);
        collect(menu, code, application, viewRoutes, permissions);
        return Collections.unmodifiableSet(permissions);
    }

    private void collectViewRoutes(JsonNode node, String code, String application,
            Map<String, Set<String>> viewRoutes) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collectViewRoutes(child, code, application, viewRoutes));
            return;
        }
        if (!node.isObject()) {
            return;
        }

        if (!menuGroup(node).isEmpty() && belongsToApplication(node, code, application)
                && "VER".equals(node.path("accion").asText())) {
            String route = node.path("ruta").asText();
            if (validRoute(route)) {
                viewRoutes.computeIfAbsent(menuGroup(node), ignored -> new HashSet<>()).add(route);
            }
        }
        collectViewRoutes(node.path("permisoHijoListDto"), code, application, viewRoutes);
    }

    private void collect(JsonNode node, String code, String application,
            Map<String, Set<String>> viewRoutes, Set<String> permissions) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> collect(child, code, application, viewRoutes, permissions));
            return;
        }
        if (!node.isObject()) {
            return;
        }

        ObjectNode menuItem = (ObjectNode) node;
        menuItem.remove("permissionRoute");
        menuItem.remove("uuid");

        if (belongsToApplication(menuItem, code, application)) {
            String route = menuItem.path("ruta").asText();
            String group = menuGroup(menuItem);
            Set<String> menuViewRoutes = group.isEmpty() ? null : viewRoutes.get(group);
            // Action routes can point to shared pages; scope them to the menu's unique VER route.
            if (menuViewRoutes != null && menuViewRoutes.size() == 1) {
                route = menuViewRoutes.iterator().next();
            }
            String action = menuItem.path("accion").asText();
            if (validRoute(route) && !action.isBlank()) {
                menuItem.put("permissionRoute", route);
                permissions.add(key(route, action));
            }
        }

        collect(menuItem.path("permisoHijoListDto"), code, application, viewRoutes, permissions);
    }

    private boolean belongsToApplication(JsonNode item, String code, String application) {
        return application.equals(item.path("modulo").asText())
                && code.equals(item.path("codigo").asText());
    }

    private String menuGroup(JsonNode item) {
        String name = item.path("nombreMenu").asText();
        if (name.isBlank()) return "";
        return item.path("modulo").asText() + "\u0000"
                + item.path("codigo").asText() + "\u0000"
                + name;
    }

    private static boolean validRoute(String route) {
        return route != null && route.startsWith("/") && !route.startsWith("//");
    }
}