package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PermissionCatalogTest {
    private final ObjectMapper mapper = new ObjectMapper();
    @Test void readsNestedAndFlatActionsWithoutGrantingParentMetadata() throws Exception {
        String permission = "{\"modulo\":\"DEPORTESAPP\",\"codigo\":\"SIS-013\","
                + "\"nombreMenu\":\"Categorias\",\"accion\":\"ACTUALIZAR\",\"estado\":false}";
        Set<String> keys = PermissionCatalog.fromMenu(mapper.readTree("[" + permission + ","
                + "{\"modulo\":\"DEPORTESAPP\",\"codigo\":\"SIS-013\",\"permisoHijoListDto\":["
                + permission + "],\"permisoPadreDto\":{\"nombreMenu\":\"Categorias\",\"accion\":\"ANULAR\"}}]"),
                "SIS-013", "DEPORTESAPP");
        assertEquals(Set.of("cat:u"), keys);
    }
    @Test void rejectsOtherApplicationsUnknownActionsAndLegacyActionNames() throws Exception {
        assertTrue(PermissionCatalog.fromMenu(mapper.readTree("[{\"modulo\":\"OTHER\",\"codigo\":\"SIS-013\","
                + "\"nombreMenu\":\"Categorias\",\"accion\":\"CREAR\"}]"), "SIS-013", "DEPORTESAPP").isEmpty());
        assertEquals("", PermissionCatalog.key("Categorias", "EDITAR"));
        assertEquals("", PermissionCatalog.key("Categorias", "ELIMINAR"));
        assertEquals("", PermissionCatalog.key("Administracion", ""));
    }
}
