package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts any source menu into the single response shape consumed by the UI.
 *
 * <p>Usuarios remains the source for staff and administrators. The taxpayer
 * JSON remains the source for taxpayers. This class only removes transport
 * identifiers and repeated action rows, then builds one navigation node per
 * module. The remaining module actions are nested authorization entries and
 * never become duplicate sidebar links.</p>
 */
@Component
public class MenuResponseNormalizer {
    private final ObjectMapper mapper;

    public MenuResponseNormalizer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public ArrayNode normalize(JsonNode source) {
        List<ActionSource> actions = new ArrayList<>();
        collect(source, null, actions);

        Map<String, String> viewRoutes = new LinkedHashMap<>();
        for (ActionSource action : actions) {
            if ("VER".equals(action.action())) {
                viewRoutes.putIfAbsent(action.identity(), routeOf(action.item()));
            }
        }

        Map<String, Module> modules = new LinkedHashMap<>();
        Set<String> seenActions = new LinkedHashSet<>();
        for (ActionSource action : actions) {
            String permissionRoute = text(action.item(), "permissionRoute");
            if (permissionRoute.isBlank()) {
                permissionRoute = viewRoutes.getOrDefault(action.identity(), routeOf(action.item()));
            }
            if (permissionRoute.isBlank()) continue;

            final String resolvedPermissionRoute = permissionRoute;
            String moduleKey = action.parentKey() + "|" + text(action.item(), "nombreMenu") + "|" + resolvedPermissionRoute;
            String actionKey = moduleKey + "|" + action.action();
            if (!seenActions.add(actionKey)) continue;

            Module module = modules.computeIfAbsent(moduleKey,
                    ignored -> new Module(action.parent(), action.item(), resolvedPermissionRoute));
            module.actions.add(new ActionSource(action.item(), action.parent(), resolvedPermissionRoute));
        }

        Map<String, ObjectNode> parents = new LinkedHashMap<>();
        List<ObjectNode> roots = new ArrayList<>();
        for (Module module : modules.values()) {
            ObjectNode moduleNode = moduleNode(module);
            if (module.parent == null) {
                roots.add(moduleNode);
                continue;
            }
            String parentKey = parentKey(module.parent);
            ObjectNode parent = parents.computeIfAbsent(parentKey, ignored -> parentNode(module.parent));
            ObjectNode parentMetadata = parent.deepCopy();
            parentMetadata.putArray("permisoHijoListDto");
            moduleNode.set("permisoPadreDto", parentMetadata);
            ((ArrayNode) parent.get("permisoHijoListDto")).add(moduleNode);
        }
        roots.addAll(parents.values());
        roots.sort(menuOrder());
        roots.forEach(this::sortChildren);

        ArrayNode result = mapper.createArrayNode();
        roots.forEach(result::add);
        return result;
    }

    private void collect(JsonNode node, JsonNode inheritedParent, List<ActionSource> actions) {
        if (node == null || node.isNull()) return;
        if (node.isArray()) {
            node.forEach(item -> collect(item, inheritedParent, actions));
            return;
        }
        if (!node.isObject()) return;

        JsonNode ownParent = node.path("permisoPadreDto").isObject() ? node.path("permisoPadreDto") : inheritedParent;
        String action = text(node, "accion");
        if (!action.isBlank() && !text(node, "nombreMenu").isBlank()) {
            actions.add(new ActionSource((ObjectNode) node, ownParent, ""));
        }

        JsonNode children = node.path("permisoHijoListDto");
        JsonNode childParent = action.isBlank() && !text(node, "nombreMenu").isBlank() ? node : inheritedParent;
        collect(children, childParent, actions);
    }

    private ObjectNode moduleNode(Module module) {
        ActionSource primary = module.actions.stream()
                .min(Comparator.comparingInt(action -> actionOrder(action.action())))
                .orElseThrow();
        ObjectNode node = copyMenuFields(primary.item());
        node.put("accion", primary.action());
        node.put("permissionRoute", module.permissionRoute);

        module.actions.stream()
                .filter(action -> action != primary)
                .sorted(Comparator.comparingInt(action -> actionOrder(action.action())))
                .forEach(action -> {
                    ObjectNode permission = copyMenuFields(action.item());
                    permission.put("accion", action.action());
                    permission.put("permissionRoute", module.permissionRoute);
                    permission.put("permissionOnly", true);
                    ObjectNode metadata = node.deepCopy();
                    metadata.putArray("permisoHijoListDto");
                    permission.set("permisoPadreDto", metadata);
                    ((ArrayNode) node.get("permisoHijoListDto")).add(permission);
                });
        return node;
    }

    private ObjectNode parentNode(JsonNode source) {
        ObjectNode parent = copyMenuFields(source);
        parent.put("accion", "");
        parent.put("ruta", "");
        parent.remove("permissionRoute");
        return parent;
    }

    private ObjectNode copyMenuFields(JsonNode source) {
        ObjectNode item = mapper.createObjectNode();
        for (String field : new String[]{"modulo", "codigo", "nombreMenu", "ruta", "orden", "icono", "estado", "subNivel"}) {
            if (source.has(field)) item.set(field, source.get(field));
        }
        if (!item.has("estado")) item.put("estado", false);
        if (!item.has("subNivel")) item.put("subNivel", false);
        item.putArray("permisoHijoListDto");
        return item;
    }

    private void sortChildren(ObjectNode node) {
        ArrayNode children = (ArrayNode) node.get("permisoHijoListDto");
        List<ObjectNode> sorted = new ArrayList<>();
        children.forEach(child -> sorted.add((ObjectNode) child));
        sorted.sort(menuOrder());
        children.removeAll();
        sorted.forEach(child -> {
            sortChildren(child);
            children.add(child);
        });
    }

    private Comparator<ObjectNode> menuOrder() {
        return Comparator.<ObjectNode>comparingInt(this::orderOf)
                .thenComparing(node -> text(node, "nombreMenu"));
    }

    private int orderOf(JsonNode node) {
        return node.path("orden").canConvertToInt() ? node.path("orden").asInt() : Integer.MAX_VALUE;
    }

    private int actionOrder(String action) {
        if ("VER".equals(action)) return 0;
        if ("RESERVAR".equals(action)) return 1;
        if ("CREAR".equals(action)) return 2;
        return 10;
    }

    private String routeOf(JsonNode item) {
        String permissionRoute = text(item, "permissionRoute");
        return permissionRoute.isBlank() ? text(item, "ruta") : permissionRoute;
    }

    private String parentKey(JsonNode parent) {
        return parent == null ? "" : text(parent, "nombreMenu") + "|" + text(parent, "ruta")
                + "|" + parent.path("orden").asText("");
    }

    private String text(JsonNode node, String field) {
        return node == null ? "" : node.path(field).asText("").trim();
    }

    private static final class Module {
        private final JsonNode parent;
        private final String permissionRoute;
        private final List<ActionSource> actions = new ArrayList<>();

        private Module(JsonNode parent, JsonNode firstAction, String permissionRoute) {
            this.parent = parent;
            this.permissionRoute = permissionRoute;
        }
    }

    private final class ActionSource {
        private final ObjectNode item;
        private final JsonNode parent;
        private final String permissionRoute;

        private ActionSource(ObjectNode item, JsonNode parent, String permissionRoute) {
            this.item = item;
            this.parent = parent;
            this.permissionRoute = permissionRoute;
        }

        private ObjectNode item() { return item; }
        private JsonNode parent() { return parent; }
        private String action() { return text(item, "accion"); }
        private String parentKey() { return MenuResponseNormalizer.this.parentKey(parent); }
        private String identity() { return parentKey() + "|" + text(item, "nombreMenu"); }
    }
}
