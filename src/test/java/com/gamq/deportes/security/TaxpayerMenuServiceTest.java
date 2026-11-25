package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The login menu is the single authorization source for taxpayers. These tests
 * pin the composition rules: a direct capability and its request variant never
 * appear together, and staff-only actions never reach a taxpayer menu.
 */
class TaxpayerMenuServiceTest {

    private TaxpayerMenuService service;

    @BeforeEach
    void setUp() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        service = new TaxpayerMenuService(mapper, "DEPORTESAPP", "SIS-013",
                new MenuResponseNormalizer(mapper));
    }

    @Test
    void studentManagementFeatureEnablesEveryStudentAction() {
        ArrayNode menu = service.forTaxpayer(Set.of("GESTION_ESTUDIANTES"));

        Set<String> actions = actionsOf(menu, "Estudiantes");
        assertTrue(actions.containsAll(Set.of("VER", "CREAR", "HABILITAR", "DAR DE BAJA", "ACTUALIZAR")),
                "expected all student actions, got " + actions);
    }

    @Test
    void studentModuleWithoutFeatureIsReadOnly() {
        ArrayNode menu = service.forTaxpayer(Set.of());

        Set<String> actions = actionsOf(menu, "Estudiantes");
        assertEquals(Set.of("VER"), actions,
                "without GESTION_ESTUDIANTES the module must stay read-only");
    }

    @Test
    void recurrentRequestAddsRequestActionWithoutDirectCreate() {
        ArrayNode menu = service.forTaxpayer(Set.of("RECURRENT_REQUEST", "RECURRENT_MANAGEMENT"));

        Set<String> actions = actionsOf(menu, "Reservas Recurrentes");
        assertTrue(actions.contains("CREAR-SOLICITUD"), "expected the request action, got " + actions);
        assertFalse(actions.contains("CREAR"), "a request capability must not expose the direct action");
    }

    @Test
    void recurrentDirectAddsCreateWithoutRequestAction() {
        ArrayNode menu = service.forTaxpayer(Set.of("RECURRENT_CREATE", "RECURRENT_MANAGEMENT"));

        Set<String> actions = actionsOf(menu, "Reservas Recurrentes");
        assertTrue(actions.contains("CREAR"), "expected the direct action, got " + actions);
        assertFalse(actions.contains("CREAR-SOLICITUD"), "a direct capability must not expose the request action");
    }

    @Test
    void eventRequestAndDirectAreMutuallyExclusiveInTheOneTimeModule() {
        ArrayNode request = service.forTaxpayer(Set.of("EVENT_REQUEST", "PUNTUAL_MANAGEMENT"));
        Set<String> requestActions = actionsOf(request, "Reservas Puntuales");
        assertTrue(requestActions.contains("CREAR-SOLICITUD-EVENTO"), "expected " + requestActions);
        assertFalse(requestActions.contains("CREAR-EVENTO"), "expected no direct event action in " + requestActions);

        ArrayNode direct = service.forTaxpayer(Set.of("EVENT_CREATE", "PUNTUAL_MANAGEMENT"));
        Set<String> directActions = actionsOf(direct, "Reservas Puntuales");
        assertTrue(directActions.contains("CREAR-EVENTO"), "expected " + directActions);
        assertFalse(directActions.contains("CREAR-SOLICITUD-EVENTO"),
                "expected no event request action in " + directActions);
    }

    @Test
    void organizationActionsAreResolvedFromTheFeatureSet() {
        ArrayNode direct = service.forTaxpayer(Set.of("ORGANIZATION_DIRECT", "PUNTUAL_MANAGEMENT"));
        Set<String> directOneTime = actionsOf(direct, "Reservas Puntuales");
        Set<String> directRecurrent = actionsOf(direct, "Reservas Recurrentes");
        assertTrue(directOneTime.contains("CREAR-PARA-ORGANIZACIONES"));
        assertTrue(directRecurrent.contains("CREAR-PARA-ORGANIZACIONES"));
        assertFalse(directOneTime.contains("CREAR-SOLICITUD-PARA-ORGANIZACIONES"));

        ArrayNode request = service.forTaxpayer(Set.of("ORGANIZATION_REQUEST", "PUNTUAL_MANAGEMENT"));
        Set<String> requestOneTime = actionsOf(request, "Reservas Puntuales");
        Set<String> requestRecurrent = actionsOf(request, "Reservas Recurrentes");
        assertTrue(requestOneTime.contains("CREAR-SOLICITUD-PARA-ORGANIZACIONES"));
        assertTrue(requestRecurrent.contains("CREAR-SOLICITUD-PARA-ORGANIZACIONES"));
        assertFalse(requestOneTime.contains("CREAR-PARA-ORGANIZACIONES"));

        ArrayNode none = service.forTaxpayer(Set.of("PUNTUAL_MANAGEMENT"));
        Set<String> noOrganizationActions = actionsOf(none, "Reservas Puntuales");
        assertFalse(noOrganizationActions.contains("CREAR-PARA-ORGANIZACIONES"));
        assertFalse(noOrganizationActions.contains("CREAR-SOLICITUD-PARA-ORGANIZACIONES"));
    }

    @Test
    void staffOnlyActionsNeverReachTaxpayerMenus() {
        ArrayNode menu = service.forTaxpayer(Set.of(
                "RECURRENT_CREATE", "RECURRENT_MANAGEMENT",
                "EVENT_CREATE", "PUNTUAL_MANAGEMENT"));

        Set<String> everyAction = new LinkedHashSet<>();
        flatten(menu).forEach(node -> {
            String action = node.path("accion").asText("");
            if (!action.isBlank()) everyAction.add(action);
        });
        assertFalse(everyAction.contains("CAMBIAR-HORARIO"), "staff-only action leaked: " + everyAction);
        assertFalse(everyAction.contains("VALIDAR"), "staff-only action leaked: " + everyAction);
    }

    private Set<String> actionsOf(ArrayNode menu, String moduleName) {
        Set<String> actions = new LinkedHashSet<>();
        for (JsonNode node : flatten(menu)) {
            if (!moduleName.equals(node.path("nombreMenu").asText())) continue;
            if (node.path("permissionRoute").asText("").isBlank()) continue;
            String action = node.path("accion").asText("");
            if (!action.isBlank()) actions.add(action);
        }
        return actions;
    }

    private JsonNode moduleNode(ArrayNode menu, String moduleName) {
        for (JsonNode node : flatten(menu)) {
            if (moduleName.equals(node.path("nombreMenu").asText())
                    && !node.path("permissionOnly").asBoolean()
                    && !node.path("permissionRoute").asText("").isBlank()) {
                return node;
            }
        }
        throw new AssertionError("module not found: " + moduleName);
    }

    private List<JsonNode> flatten(JsonNode node) {
        List<JsonNode> nodes = new ArrayList<>();
        if (node == null) return nodes;
        if (node.isArray()) {
            node.forEach(child -> nodes.addAll(flatten(child)));
            return nodes;
        }
        if (!node.isObject()) return nodes;
        nodes.add(node);
        nodes.addAll(flatten(node.path("permisoHijoListDto")));
        return nodes;
    }
}
