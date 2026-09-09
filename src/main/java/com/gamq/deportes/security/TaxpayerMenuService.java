package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class TaxpayerMenuService {
    private final ArrayNode template;

    public TaxpayerMenuService(ObjectMapper mapper) throws IOException {
        try (InputStream stream = new ClassPathResource("security/taxpayer-menu.json").getInputStream()) {
            template = (ArrayNode) mapper.readTree(stream);
        }
    }

    public ArrayNode forRepresentative(boolean representative) {
        ArrayNode menu = template.deepCopy();
        if (!representative) removeRepresentativeActions(menu);
        return menu;
    }

    private void removeRepresentativeActions(ArrayNode items) {
        Iterator<JsonNode> iterator = items.iterator();
        while (iterator.hasNext()) {
            JsonNode item = iterator.next();
            String action = item.path("accion").asText();
            if ("Escuelas Municipales".equals(item.path("nombreMenu").asText())
                    && ("CREAR".equals(action) || "ACTUALIZAR".equals(action))) {
                iterator.remove();
            } else if (item.path("permisoHijoListDto").isArray()) {
                removeRepresentativeActions((ArrayNode) item.get("permisoHijoListDto"));
            }
        }
    }
}
