package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** One editable definition for the taxpayer menu. The frontend consumes the resolved actions only. */
@Service
public class TaxpayerMenuService {
    private final JsonNode definition;
    private final ObjectMapper mapper;
    private final String application;
    private final String code;
    private final MenuResponseNormalizer menuNormalizer;

    public TaxpayerMenuService(ObjectMapper mapper,
            @Value("${app.name}") String application,
            @Value("${app.code}") String code,
            MenuResponseNormalizer menuNormalizer) throws IOException {
        this.mapper = mapper;
        this.application = application;
        this.code = code;
        this.menuNormalizer = menuNormalizer;
        try (InputStream input = new ClassPathResource("security/taxpayer-menu.json").getInputStream()) {
            definition = mapper.readTree(input);
        }
    }

    public ArrayNode forTaxpayer(Set<String> enabledFeatures) {
        ArrayNode rawMenu = mapper.createArrayNode();
        Map<String, ObjectNode> sections = new LinkedHashMap<>();

        for (JsonNode section : definition.path("sections")) {
            ObjectNode item = base(section);
            item.put("accion", "").put("ruta", "");
            sections.put(item.path("nombreMenu").asText(), item);
        }

        for (JsonNode module : definition.path("modules")) {
            List<JsonNode> enabledActions = enabledActions(module, enabledFeatures);
            JsonNode viewAction = enabledActions.stream()
                    .filter(action -> "VER".equals(action.path("accion").asText()))
                    .findFirst()
                    .orElse(null);
            if (viewAction == null) {
                continue;
            }

            ObjectNode moduleItem = actionItem(module, viewAction, enabledFeatures);
            ObjectNode parent = sections.get(module.path("section").asText());
            if (parent != null) {
                ObjectNode metadata = parent.deepCopy();
                metadata.putArray("permisoHijoListDto");
                moduleItem.set("permisoPadreDto", metadata);
            }
            rawMenu.add(moduleItem);

            for (JsonNode action : enabledActions) {
                if (action == viewAction) {
                    continue;
                }
                ObjectNode permission = actionItem(module, action, enabledFeatures);
                if (parent != null) {
                    ObjectNode metadata = parent.deepCopy();
                    metadata.putArray("permisoHijoListDto");
                    permission.set("permisoPadreDto", metadata);
                }
                rawMenu.add(permission);
            }
        }
        return menuNormalizer.normalize(rawMenu);
    }

    private List<JsonNode> enabledActions(JsonNode module, Set<String> enabledFeatures) {
        List<JsonNode> actions = new ArrayList<>();
        for (JsonNode action : module.path("actions")) {
            if (action.path("staffOnly").asBoolean()) {
                continue;
            }
            String requiredFeature = action.path("requiredFeature").asText("");
            if (requiredFeature.isBlank() || enabledFeatures.contains(requiredFeature)) {
                actions.add(action);
            }
        }
        return actions;
    }

    private ObjectNode actionItem(JsonNode module, JsonNode action, Set<String> enabledFeatures) {
        ObjectNode item = base(module);
        item.put("accion", action.path("accion").asText());
        item.put("ruta", action.path("ruta").asText(module.path("ruta").asText()));
        return item;
    }

    /**
     * Compatibility helper for callers that need the complete static taxpayer
     * catalogue (not the runtime login menu). Production authentication uses
     * {@link #forTaxpayer(Set)} with resolved features.
     */
    public ArrayNode forRepresentative(boolean representative) {
        java.util.Set<String> features = new java.util.HashSet<>(Set.of(
                "RECURRENT_CREATE", "RECURRENT_REQUEST", "RECURRENT_MANAGEMENT",
                "PUNTUAL_MANAGEMENT",
                "EVENT_CREATE", "EVENT_REQUEST",
                "GESTION_MENSUALIDADES", "GESTION_BORDEROUX", "GESTION_ESTUDIANTES"));
        if (representative) {
            features.add("ORGANIZATION_DIRECT");
        }
        return forTaxpayer(features);
    }

    private ObjectNode base(JsonNode source) {
        ObjectNode item = mapper.createObjectNode();
        for (String field : new String[]{"nombreMenu", "ruta", "orden", "icono"}) {
            if (source.has(field)) {
                item.set(field, source.get(field));
            }
        }
        item.put("modulo", application)
                .put("codigo", code)
                .put("estado", false)
                .put("subNivel", false);
        item.putArray("permisoHijoListDto");
        return item;
    }
}
