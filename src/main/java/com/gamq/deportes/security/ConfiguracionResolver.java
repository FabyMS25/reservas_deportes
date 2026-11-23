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
 * <p>A configuration is identified by its key together with its scope (and,
 * when used, its reference). A key may therefore exist in more than one
 * scope. Permission flags are resolved only during taxpayer login using:
 * representante &gt; contribuyente &gt; global.</p>
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>Staff/admin actions come from Usuarios and the assigned-complex rule.</li>
 *   <li>Taxpayer permission flags become signed menu permissions at login.</li>
 *   <li>A non-representative never receives representative-only flags.</li>
 *   <li>Numeric and text policies are resolved from the applicable active
 *       candidates; current reservation and payment policies are global.</li>
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
     * Active rows for a key that apply to the caller. Entity overrides are not
     * enabled here: only rows without referenciaUuid are considered.
     */
    @Transactional(readOnly = true)
    public List<ConfiguracionSistema> candidatos(String clave) {
        if (clave == null || clave.isBlank()) {
            return List.of();
        }
        return configuraciones.findAudienceCandidates(clave, audienciasAplicables());
    }

    /**
     * Resolves one taxpayer feature while building the signed menu at login.
     * No request handler calls this method for permission authorization.
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
