package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;

/** Stable, compact permission keys for the signed Deportes identity. Menu metadata is not authority. */
public final class PermissionCatalog {
    private PermissionCatalog() {}
    private static final Map<String, String> MODULES = Map.ofEntries(
        Map.entry("Dashboard de Admin", "dash"), Map.entry("Calendario", "cal"),
        Map.entry("Mapa", "map"), Map.entry("Contribuyentes", "tax"),
        Map.entry("Organizaciones", "org"), Map.entry("Estudiantes", "stu"),
        Map.entry("Reservas Puntuales", "one"), Map.entry("Reservas Recurrentes", "rec"),
        Map.entry("Escuelas Municipales", "sch"), Map.entry("Campos Deportivos", "field"),
        Map.entry("Complejos Deportivos", "complex"), Map.entry("Encargados", "staff"),
        Map.entry("Categorias", "cat"), Map.entry("Superficies", "surface"),
        Map.entry("Clasificadores Ruat", "class"), Map.entry("Configuraciones de Sistema", "config"),
        Map.entry("Reporte de Ingresos", "rp"), Map.entry("Reporte de Mensualidades", "rm"),
        Map.entry("Reporte de Reservas Puntuales", "ro"), Map.entry("Reporte de Reservas Recurrentes", "rr"));
    private static final Map<String, String> ACTIONS = Map.ofEntries(
        Map.entry("VER", "v"), Map.entry("CREAR", "c"), Map.entry("ACTUALIZAR", "u"),
        Map.entry("ANULAR", "a"), Map.entry("VALIDAR", "valid"), Map.entry("GENERAR TICKET", "ticket"),
        Map.entry("CAMBIAR-HORARIO", "time"), Map.entry("EXPORTAR", "export"),
        Map.entry("BLOQUEAR", "block"), Map.entry("RESERVAR", "book"), Map.entry("GENERAR", "gen"),
        Map.entry("DAR DE BAJA", "off"), Map.entry("HABILITAR", "on"),
        Map.entry("CREAR-POSTPAGO", "post"), Map.entry("NOTIFICACIONES_ADMIN", "notify"),
        Map.entry("ACTUALIZAR_PROTEGIDA", "protected"), Map.entry("ACTUALIZAR_CONTROL", "control"));

    public static String key(String module, String action) {
        String m = MODULES.get(module), a = ACTIONS.get(action);
        return m == null || a == null ? "" : m + ":" + a;
    }

    public static Set<String> fromMenu(JsonNode menu, String applicationCode, String applicationName) {
        Set<String> result = new TreeSet<>();
        collect(menu, applicationCode, applicationName, result);
        return Collections.unmodifiableSet(result);
    }

    private static void collect(JsonNode node, String code, String name, Set<String> result) {
        if (node == null) return;
        if (node.isArray()) {
            node.forEach(child -> collect(child, code, name, result));
        } else if (node.isObject()) {
            if (!name.equals(node.path("modulo").asText())
                    || !code.equals(node.path("codigo").asText())) return;
            String key = key(node.path("nombreMenu").asText(), node.path("accion").asText());
            if (!key.isEmpty()) result.add(key);
            // Parent DTOs describe navigation only; they must never grant permissions.
            collect(node.path("permisoHijoListDto"), code, name, result);
        }
    }
}
