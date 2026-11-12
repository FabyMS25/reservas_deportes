package com.gamq.deportes.security;

import com.gamq.deportes.model.ConfiguracionSistema;
import com.gamq.deportes.repository.ConfiguracionSistemaRepository;
import com.gamq.deportes.repository.OrganizacionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Single resolver for `configuracion_sistema`.
 *
 * <p>Only the PERMISSIONS category has an audience precedence:
 * representante (only for contributors that represent at least one
 * organization) &gt; contribuyente (any contributor) &gt; global. Every other
 * category is a plain parameter: `clave` is unique, so a key has exactly one
 * row and `aplica_a` only states who may read it. There is no precedence
 * between scopes for those categories.</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Admin is never restricted by a functionality flag.</li>
 *   <li>Staff is not gated by flags either: menu permissions plus the
 *       assigned complex decide what they can do.</li>
 *   <li>A contributor without an organization never receives
 *       `representante` values, so those functionalities are read-only for
 *       them (missing/inapplicable scope resolves to {@code false}).</li>
 *   <li>Numeric/text values are parameters: each key has a single row, so the
 *       value is returned as-is when its scope applies to the caller.</li>
 * </ul>
 */
@Component
public class ConfiguracionResolver {

    public static final String GLOBAL = "global";
    public static final String CONTRIBUYENTE = "contribuyente";
    public static final String REPRESENTANTE = "representante";

    private final ConfiguracionSistemaRepository configuraciones;
    private final OrganizacionRepository organizaciones;

    public ConfiguracionResolver(ConfiguracionSistemaRepository configuraciones,
                                 OrganizacionRepository organizaciones) {
        this.configuraciones = configuraciones;
        this.organizaciones = organizaciones;
    }

    public DeportesPrincipal principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof DeportesPrincipal
                ? (DeportesPrincipal) authentication.getPrincipal() : null;
    }

    public boolean esStaff() {
        DeportesPrincipal p = principal();
        return p != null && "STAFF".equalsIgnoreCase(p.getUserType());
    }

    public boolean esAdmin() {
        DeportesPrincipal p = principal();
        return p != null && "STAFF".equalsIgnoreCase(p.getUserType()) && p.isAdmin();
    }

    public boolean esContribuyente() {
        DeportesPrincipal p = principal();
        return p != null && "TAXPAYER".equalsIgnoreCase(p.getUserType());
    }

    @Transactional(readOnly = true)
    public boolean representaOrganizacion() {
        DeportesPrincipal p = principal();
        return p != null && esContribuyente()
                && organizaciones.countByRepresentanteUuid(p.getSubject()) > 0;
    }

    /** Audiences that apply to the current caller, from most to least specific. */
    public List<String> audienciasAplicables() {
        DeportesPrincipal p = principal();
        if (p == null || esStaff()) {
            // Internal users and background jobs only read global parameters.
            return List.of(GLOBAL);
        }
        List<String> scopes = new ArrayList<>(4);
        if (representaOrganizacion()) {
            scopes.add(REPRESENTANTE);
        }
        scopes.add(CONTRIBUYENTE);
        scopes.add(GLOBAL);
        return scopes;
    }

    /**
     * Active audience rows for a key that apply to the caller. Only
     * PERMISSIONS are ordered by precedence; other categories have a single
     * row per key. Entity overrides are not enabled: only rows without
     * referenciaUuid are considered.
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionSistema> candidatos(String clave) {
        if (clave == null || clave.isBlank()) {
            return List.of();
        }
        return configuraciones.findAudienceCandidates(clave, audienciasAplicables());
    }

    /**
     * Functionality flag. Admin/staff are never blocked (menu permissions and
     * assigned complex handle them). A contributor receives the most specific
     * applicable row; without a row the functionality is disabled.
     */
    @Transactional(readOnly = true)
    public boolean permisoHabilitado(String clave) {
        if (esAdmin() || esStaff()) {
            return true;
        }
        return permisoHabilitadoParaAudiencia(clave, representaOrganizacion());
    }

    /**
     * Resolves a contributor feature before an authenticated DeportesPrincipal
     * exists (for example while building the signed taxpayer menu at login).
     * The precedence is the same as {@link #permisoHabilitado(String)}:
     * representante, contribuyente, then global.
     */
    @Transactional(readOnly = true)
    public boolean permisoHabilitadoParaAudiencia(String clave, boolean representante) {
        if (clave == null || clave.isBlank()) {
            return false;
        }
        List<String> audiencias = new ArrayList<>(3);
        if (representante) {
            audiencias.add(REPRESENTANTE);
        }
        audiencias.add(CONTRIBUYENTE);
        audiencias.add(GLOBAL);
        return configuraciones.findAudienceCandidates(clave, audiencias).stream()
                .findFirst()
                .map(ConfiguracionSistema::getValorBoolean)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Double valorNumerico(String clave, Double porDefecto) {
        return candidatos(clave).stream()
                .map(ConfiguracionSistema::getValorNumerico)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(porDefecto);
    }

    @Transactional(readOnly = true)
    public Boolean valorBoolean(String clave, Boolean porDefecto) {
        return candidatos(clave).stream()
                .map(ConfiguracionSistema::getValorBoolean)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(porDefecto);
    }

    @Transactional(readOnly = true)
    public String valorTexto(String clave, String porDefecto) {
        return candidatos(clave).stream()
                .map(ConfiguracionSistema::getValorTexto)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(porDefecto);
    }
}
