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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        TaxpayerMenuService menus = new TaxpayerMenuService(
                mapper, "DEPORTESAPP", "SIS-013", new MenuResponseNormalizer(mapper));

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
    void returnsOneNavigationTreeWithoutDuplicatingActionsAtTheRoot() throws IOException {
        TaxpayerMenuService menus = new TaxpayerMenuService(
                mapper, "DEPORTESAPP", "SIS-013", new MenuResponseNormalizer(mapper));

        ArrayNode menu = menus.forRepresentative(true);

        JsonNode reservations = null;
        for (JsonNode item : menu) {
            if ("Reservas".equals(item.path("nombreMenu").asText())) {
                reservations = item;
            }
            assertFalse("Reservas Puntuales".equals(item.path("nombreMenu").asText()));
        }
        assertTrue(reservations != null);
        JsonNode oneTime = reservations.path("permisoHijoListDto").get(0);
        assertEquals("Reservas Puntuales", oneTime.path("nombreMenu").asText());
        assertEquals("VER", oneTime.path("accion").asText());
        assertTrue(oneTime.path("permisoHijoListDto").get(0).path("permissionOnly").asBoolean());
    }

    @Test
    void keepsOrdinaryOneTimeCreationWithoutAnOrganizationCapability() throws IOException {
        TaxpayerMenuService menus = new TaxpayerMenuService(
                mapper, "DEPORTESAPP", "SIS-013", new MenuResponseNormalizer(mapper));

        ArrayNode ordinaryTaxpayerMenu = menus.forTaxpayer(Set.of("PUNTUAL_MANAGEMENT"));
        ArrayNode representativeMenu = menus.forTaxpayer(Set.of("PUNTUAL_MANAGEMENT", "ORGANIZATION_DIRECT"));

        JsonNode ordinary = findByPermissionRoute(ordinaryTaxpayerMenu, "/reservations/one-time");
        JsonNode representative = findByPermissionRoute(representativeMenu, "/reservations/one-time");
        assertTrue(catalog.fromMenu(ordinaryTaxpayerMenu, "SIS-013", "DEPORTESAPP")
                .contains(PermissionCatalog.key("/reservations/one-time", "CREAR")));
        assertFalse(catalog.fromMenu(ordinaryTaxpayerMenu, "SIS-013", "DEPORTESAPP")
                .contains(PermissionCatalog.key("/reservations/one-time", "CREAR-PARA-ORGANIZACIONES")));
        assertTrue(catalog.fromMenu(representativeMenu, "SIS-013", "DEPORTESAPP")
                .contains(PermissionCatalog.key("/reservations/one-time", "CREAR-PARA-ORGANIZACIONES")));
    }

    @Test
    void normalizesTheUsuariosMenuWithoutReplacingItsPermissions() {
        ObjectNode parent = action("Reservas", "", "");
        parent.put("uuid", "usuarios-parent-id");
        ObjectNode view = action("Reservas Puntuales", "VER", "/reservations/one-time");
        view.set("permisoPadreDto", parent.deepCopy());
        ObjectNode create = action("Reservas Puntuales", "CREAR", "/reservations/details");
        create.set("permisoPadreDto", parent.deepCopy());
        ArrayNode source = mapper.createArrayNode().add(parent).add(view).add(create).add(create.deepCopy());

        ArrayNode normalized = new MenuResponseNormalizer(mapper).normalize(source);

        assertEquals(1, normalized.size());
        assertFalse(normalized.get(0).has("uuid"));
        JsonNode module = normalized.get(0).path("permisoHijoListDto").get(0);
        assertEquals("Reservas Puntuales", module.path("nombreMenu").asText());
        assertEquals("VER", module.path("accion").asText());
        assertEquals(1, module.path("permisoHijoListDto").size());
        assertEquals("CREAR", module.path("permisoHijoListDto").get(0).path("accion").asText());
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

    private JsonNode findByPermissionRoute(JsonNode node, String permissionRoute) {
        if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findByPermissionRoute(child, permissionRoute);
                if (found != null) return found;
            }
        } else if (node.isObject()) {
            if (permissionRoute.equals(node.path("permissionRoute").asText())) return node;
            return findByPermissionRoute(node.path("permisoHijoListDto"), permissionRoute);
        }
        return null;
    }
}
