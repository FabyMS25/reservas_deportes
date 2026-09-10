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
import java.util.LinkedHashMap;
import java.util.Map;

/** One editable definition per taxpayer menu. The frontend never rebuilds this policy. */
@Service
public class TaxpayerMenuService {
    private final JsonNode definition;
    private final ObjectMapper mapper;
    private final String application;
    private final String code;

    public TaxpayerMenuService(ObjectMapper mapper,
            @Value("${deportes.application.name}") String application,
            @Value("${deportes.application.code}") String code) throws IOException {
        this.mapper = mapper; this.application = application; this.code = code;
        try (InputStream in = new ClassPathResource("security/taxpayer-menu.json").getInputStream()) {
            definition = mapper.readTree(in);
        }
    }

    public ArrayNode forRepresentative(boolean representative) {
        ArrayNode menu = mapper.createArrayNode();
        Map<String, ObjectNode> sections = new LinkedHashMap<>();
        for (JsonNode section : definition.path("sections")) {
            ObjectNode item = base(section);
            item.put("accion", "").put("ruta", "");
            sections.put(item.path("uuid").asText(), item);
        }
        for (JsonNode module : definition.path("modules")) {
            for (JsonNode action : module.path("actions")) {
                if (action.path("representativeOnly").asBoolean() && !representative) continue;
                ObjectNode item = base(module);
                item.put("uuid", action.path("uuid").asText());
                item.put("accion", action.path("accion").asText());
                item.put("ruta", action.path("ruta").asText(module.path("ruta").asText()));
                ObjectNode parent = sections.get(module.path("section").asText());
                if (parent != null) {
                    ObjectNode metadata = parent.deepCopy();
                    metadata.putArray("permisoHijoListDto");
                    item.set("permisoPadreDto", metadata);
                    ((ArrayNode) parent.get("permisoHijoListDto")).add(item.deepCopy());
                }
                menu.add(item);
            }
        }
        sections.values().forEach(menu::add);
        return menu;
    }

    private ObjectNode base(JsonNode source) {
        ObjectNode item = mapper.createObjectNode();
        for (String field : new String[]{"uuid", "nombreMenu", "ruta", "orden", "icono"})
            if (source.has(field)) item.set(field, source.get(field));
        item.put("modulo", application).put("codigo", code).put("estado", false).put("subNivel", false);
        item.putArray("permisoHijoListDto");
        return item;
    }
}
