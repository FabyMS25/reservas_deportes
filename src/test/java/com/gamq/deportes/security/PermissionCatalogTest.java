package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PermissionCatalogTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final PermissionCatalog catalog = new PermissionCatalog();

    @Test
    void scopesActionsToTheMenuViewRouteWithoutChangingTheirNavigationRoutes() {
        ArrayNode menu = mapper.createArrayNode();
        menu.add(action("Reservas Puntuales", "VER", "/reservations/one-time"));
        menu.add(action("Reservas Puntuales", "CREAR", "/reservations/details"));
        menu.add(action("Reservas Puntuales", "GENERAR TICKET", "/reservations/ticket"));
        menu.add(action("Reservas Recurrentes", "VER", "/reservations/recurrent"));
        menu.add(action("Reservas Recurrentes", "CREAR", "/reservations/details"));
        menu.add(action("Reservas Recurrentes", "GENERAR TICKET", "/reservations/ticket"));

        Set<String> permissions = catalog.fromMenu(menu, "SIS-013", "DEPORTESAPP");

        assertEquals(Set.of(
                PermissionCatalog.key("/reservations/one-time", "VER"),
                PermissionCatalog.key("/reservations/one-time", "CREAR"),
                PermissionCatalog.key("/reservations/one-time", "GENERAR TICKET"),
                PermissionCatalog.key("/reservations/recurrent", "VER"),
                PermissionCatalog.key("/reservations/recurrent", "CREAR"),
                PermissionCatalog.key("/reservations/recurrent", "GENERAR TICKET")
        ), permissions);
        assertEquals("/reservations/details", menu.get(1).path("ruta").asText());
        assertEquals("/reservations/one-time", menu.get(1).path("permissionRoute").asText());
        assertEquals("/reservations/ticket", menu.get(2).path("ruta").asText());
        assertEquals("/reservations/one-time", menu.get(2).path("permissionRoute").asText());
        assertEquals("/reservations/recurrent", menu.get(4).path("permissionRoute").asText());
        assertEquals("/reservations/recurrent", menu.get(5).path("permissionRoute").asText());
    }

    @Test
    void keepsActionRouteWhenMenuHasNoUniqueViewRoute() {
        ArrayNode menu = mapper.createArrayNode();
        menu.add(action("Reportes", "GENERAR", "/reports/generate"));

        Set<String> permissions = catalog.fromMenu(menu, "SIS-013", "DEPORTESAPP");

        assertTrue(permissions.contains(PermissionCatalog.key("/reports/generate", "GENERAR")));
        assertEquals("/reports/generate", menu.get(0).path("permissionRoute").asText());
    }

    @Test
    void doesNotGuessWhenMenuHasConflictingViewRoutes() {
        ArrayNode menu = mapper.createArrayNode();
        menu.add(action("Reservas", "VER", "/reservations/one-time"));
        menu.add(action("Reservas", "VER", "/reservations/recurrent"));
        menu.add(action("Reservas", "CREAR", "/reservations/details"));

        Set<String> permissions = catalog.fromMenu(menu, "SIS-013", "DEPORTESAPP");

        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/details", "CREAR")));
        assertEquals("/reservations/details", menu.get(2).path("permissionRoute").asText());
    }

    @Test
    void scopesEveryConfiguredTaxpayerActionToItsUniqueViewRoute() throws IOException {
        TaxpayerMenuService menus = new TaxpayerMenuService(mapper, "DEPORTESAPP", "SIS-013");

        ArrayNode menu = menus.forRepresentative(true);
        Set<String> permissions = catalog.fromMenu(menu, "SIS-013", "DEPORTESAPP");

        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/one-time", "CREAR")));
        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/one-time", "GENERAR TICKET")));
        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/recurrent", "CREAR")));
        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/recurrent", "GENERAR TICKET")));
        assertTrue(permissions.contains(PermissionCatalog.key("/reservations/schools", "GENERAR TICKET")));

        List<ObjectNode> items = new ArrayList<>();
        collectItems(menu, items);
        Map<String, Set<String>> viewRoutes = new HashMap<>();
        for (ObjectNode item : items) {
            if ("VER".equals(item.path("accion").asText())) {
                viewRoutes.computeIfAbsent(item.path("nombreMenu").asText(), ignored -> new HashSet<>())
                        .add(item.path("ruta").asText());
            }
        }
        for (ObjectNode item : items) {
            String action = item.path("accion").asText();
            if (action.isBlank()) continue;

            String route = item.path("ruta").asText();
            Set<String> menuRoutes = viewRoutes.get(item.path("nombreMenu").asText());
            if (menuRoutes != null && menuRoutes.size() == 1) {
                route = menuRoutes.iterator().next();
            }
            assertEquals(route, item.path("permissionRoute").asText());
            assertTrue(permissions.contains(PermissionCatalog.key(route, action)));
        }
    }

    @Test
    void continuesCollectingMatchingPermissionsBelowAnUnrelatedParent() {
        ArrayNode menu = mapper.createArrayNode();
        ObjectNode wrapper = action("Other module", "", "");
        wrapper.put("modulo", "OTHER");
        wrapper.put("codigo", "OTHER");
        wrapper.withArray("permisoHijoListDto").add(action("Fields", "VER", "/settings/fields"));

        menu.add(wrapper);
        Set<String> permissions = catalog.fromMenu(menu, "SIS-013", "DEPORTESAPP");

        assertTrue(permissions.contains(PermissionCatalog.key("/settings/fields", "VER")));
        assertEquals("/settings/fields",
                wrapper.path("permisoHijoListDto").get(0).path("permissionRoute").asText());
    }

    private ObjectNode action(String name, String action, String route) {
        ObjectNode item = mapper.createObjectNode();
        item.put("modulo", "DEPORTESAPP");
        item.put("codigo", "SIS-013");
        item.put("nombreMenu", name);
        item.put("accion", action);
        item.put("ruta", route);
        item.putArray("permisoHijoListDto");
        return item;
    }

    private void collectItems(JsonNode node, List<ObjectNode> items) {
        if (node.isArray()) {
            node.forEach(child -> collectItems(child, items));
        } else if (node.isObject()) {
            items.add((ObjectNode) node);
            collectItems(node.path("permisoHijoListDto"), items);
        }
    }
}
