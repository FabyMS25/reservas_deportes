package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gamq.deportes.dto.ContribuyenteDto;
import com.gamq.deportes.dto.EncargadoComplejoDto;
import com.gamq.deportes.dto.auth.AuthenticationRequest;
import com.gamq.deportes.dto.auth.AuthenticationResponse;
import com.gamq.deportes.model.login.login;
import com.gamq.deportes.security.DeportesTokenService;
import com.gamq.deportes.security.PermissionCatalog;
import com.gamq.deportes.security.TaxpayerMenuService;
import com.gamq.deportes.security.MenuResponseNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.time.Instant;

@Service
public class AuthenticationService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ContribuyenteService taxpayerService;
    private final EncargadoComplejoService staffAssignmentService;
    private final DeportesTokenService tokenService;
    private final UsuariosSessionService usuariosSessions;
    private final String usersApiUrl;
    private final String applicationCode;
    private final String applicationName;
    private final String adminMenu;
    private final TaxpayerMenuService taxpayerMenus;
    private final MenuResponseNormalizer menuNormalizer;
    private final PermissionCatalog permissionCatalog;
    private final ApiService ruatService;
    private final ConfiguracionSistemaService configuracionSistemaService;
    private final com.gamq.deportes.security.ConfiguracionResolver configuracionResolver;

    public AuthenticationService(RestTemplate restTemplate, ObjectMapper objectMapper,
            ContribuyenteService taxpayerService, EncargadoComplejoService staffAssignmentService,
            DeportesTokenService tokenService, UsuariosSessionService usuariosSessions,
            @Value("${usuarios.gateway.base-url}") String usersApiUrl,
            @Value("${app.code}") String applicationCode,
            @Value("${app.name}") String applicationName,
            @Value("${app.admin-menu}") String adminMenu,
            TaxpayerMenuService taxpayerMenus, MenuResponseNormalizer menuNormalizer, PermissionCatalog permissionCatalog,
            ApiService ruatService,
            ConfiguracionSistemaService configuracionSistemaService,
            com.gamq.deportes.security.ConfiguracionResolver configuracionResolver) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.taxpayerService = taxpayerService;
        this.staffAssignmentService = staffAssignmentService;
        this.tokenService = tokenService;
        this.usuariosSessions = usuariosSessions;
        this.usersApiUrl = usersApiUrl;
        this.applicationCode = applicationCode;
        this.applicationName = applicationName;
        this.adminMenu = adminMenu;
        this.taxpayerMenus = taxpayerMenus;
        this.menuNormalizer = menuNormalizer;
        this.permissionCatalog = permissionCatalog;
        this.ruatService = ruatService;
        this.configuracionSistemaService = configuracionSistemaService;
        this.configuracionResolver = configuracionResolver;
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        if (request.getUsername() == null || request.getPassword() == null)
            throw new AuthenticationFailure("BAD_CREDENTIALS", "Usuario o contraseña incorrectos");
        JsonNode availability;
        try {
            availability = ruatService.checkRuatAvailability();
            if (!availability.path("available").isBoolean()) {
                throw new AuthenticationFailure("CONFIG_ERROR", "No se pudo confirmar la conexión de Deportes con ruat-be.");
            }
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            String message = "No se recibió un mensaje válido de ruat-be.";
            try {
                JsonNode body = objectMapper.readTree(ex.getResponseBodyAsString());
                if (body != null && body.path("mensaje").isTextual()
                        && !body.path("mensaje").asText().isBlank()) {
                    message = body.path("mensaje").asText();
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {
                // A non-JSON response has no gateway message to forward.
            }
            throw new AuthenticationFailure("CONFIG_ERROR", message);
        } catch (RestClientException | IllegalStateException | IllegalArgumentException ex) {
            throw new AuthenticationFailure("CONFIG_ERROR", "No se pudo confirmar la conexión de Deportes con ruat-be. Contacte al administrador.");
        }
        AuthenticationResponse response;
        try {
            response = loginStaff(request);
        } catch (ExternalCredentialsRejected rejected) {
            try {
                response = loginTaxpayer(request);
            } catch (AuthenticationFailure taxpayerFailure) {
                if ("BAD_CREDENTIALS".equals(taxpayerFailure.getCode()) && rejected.upstreamResponse != null) {
                    if (isCredentialRejection(rejected.upstreamResponse.response)) {
                        throw new AuthenticationFailure("BAD_CREDENTIALS", "Usuario o contraseña incorrectos");
                    }
                    throw rejected.upstreamResponse;
                }
                throw taxpayerFailure;
            }
        } catch (ExternalUnavailable externalUnavailable) {
            try {
                response = loginTaxpayer(request);
            } catch (AuthenticationFailure taxpayerFailure) {
                if (!"BAD_CREDENTIALS".equals(taxpayerFailure.getCode())) throw taxpayerFailure;
                throw new AuthenticationFailure("USUARIOS_UNAVAILABLE",
                        "No se pudo validar el acceso con el sistema de Usuarios. Revise la conexión e intente nuevamente.");
            }
        }
        response.setRuatStatus(availability.path("available").asBoolean());
        response.setRuatMessage(availability.path("mensaje").asText("No se pudo confirmar la autenticación con RUAT."));
        return response;
    }

    public void logout(String userUuid, String userType) {
        if ("STAFF".equals(userType)) usuariosSessions.remove(userUuid);
    }

    private AuthenticationResponse loginStaff(AuthenticationRequest request) {
        JsonNode externalUser;
        try {
            externalUser = restTemplate.postForObject(usersApiUrl + "/auth/signin",
                    Map.of("username", request.getUsername(), "password", request.getPassword()), JsonNode.class);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            throw new ExternalCredentialsRejected(new UpstreamResponseException(ex));
        } catch (RestClientException ex) {
            throw new ExternalUnavailable();
        }
        externalUser = unwrap(externalUser);
        if (externalUser == null || !externalUser.hasNonNull("token")) throw new ExternalCredentialsRejected(null);
        if (!containsText(externalUser.path("modulos"), applicationName))
            throw new AuthenticationFailure("NO_MODULE", "No tiene acceso a este módulo");

        String userUuid = externalUser.path("uuid").asText("");
        if (userUuid.isBlank()) throw new AuthenticationFailure("BAD_CREDENTIALS", "Respuesta inválida del sistema de usuarios");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(externalUser.path("token").asText());
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = usersApiUrl + "/permiso/permisos-porusuario?uuidUsuario=" + userUuid
                + "&codigoModulo=" + applicationCode;
        JsonNode permissions;
        try {
            permissions = unwrap(restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class).getBody());
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            throw new UpstreamResponseException(ex);
        } catch (RestClientException ex) {
            throw new AuthenticationFailure("USUARIOS_UNAVAILABLE",
                    "No se pudo consultar los permisos en el sistema de Usuarios");
        }
        if (permissions == null || !permissions.isArray() || permissions.isEmpty())
            throw new AuthenticationFailure("NO_PERMISSIONS", "No tiene permisos para acceder a este módulo");

        boolean admin = containsMenu(permissions, adminMenu);
        EncargadoComplejoDto assignment = null;
        if (!admin) {
            try { assignment = staffAssignmentService.findByUserUuid(userUuid); }
            catch (com.gamq.deportes.exception.NotFoundException ex) {
                throw new AuthenticationFailure("NO_ASSIGNATION", "No tiene el permiso de administrador del módulo ni una asignación como encargado o staff. Solicite que revisen sus permisos y asignación.");
            }
        }
        if (!admin && (assignment == null || assignment.getComplejoDto() == null
                || !Boolean.TRUE.equals(assignment.getActivo())))
            throw new AuthenticationFailure("NO_ASSIGNATION", "No tiene el permiso de administrador del módulo ni una asignación como encargado o staff. Solicite que revisen sus permisos y asignación.");
        String usuariosToken = externalUser.path("token").asText();
        long expiresAt = usuariosSessions.expirationBoundedByUpstreamToken(usuariosToken, tokenService.expirationEpoch());
        if (expiresAt <= Instant.now().getEpochSecond()) {
            throw new AuthenticationFailure("USUARIOS_UNAVAILABLE", "La sesión recibida de Usuarios ya expiró. Intente iniciar sesión nuevamente.");
        }
        String personUuid = externalUser.path("designacion").path("personaDto").path("uuid").asText("");
        ((com.fasterxml.jackson.databind.node.ObjectNode) externalUser).remove("token");
        ArrayNode menu = menuNormalizer.normalize(permissions);
        String token = tokenService.create(userUuid, "STAFF", admin,
                permissionCatalog.fromMenu(menu, applicationCode, applicationName), expiresAt);
        usuariosSessions.put(userUuid, usuariosToken, personUuid, expiresAt);
        return new AuthenticationResponse(token, externalUser, menu, assignment, admin, "STAFF",
                configuracionSistemaService.obtenerConfiguracionesParaCliente());
    }

    private AuthenticationResponse loginTaxpayer(AuthenticationRequest request) {
        ContribuyenteDto taxpayer;
        try {
            login credentials = new login();
            credentials.setUser(request.getUsername());
            credentials.setPass(request.getPassword());
            taxpayer = taxpayerService.loginContribuyente(credentials);
        } catch (com.gamq.deportes.exception.NotFoundException | IllegalArgumentException ex) {
            throw new AuthenticationFailure("BAD_CREDENTIALS", "Usuario o contraseña incorrectos");
        } catch (org.springframework.dao.DataAccessException ex) {
            throw new AuthenticationFailure("CONTRIBUYENTES_UNAVAILABLE",
                    "No se pudo validar el acceso de contribuyentes. Intente nuevamente más tarde.");
        }

        String accountState = taxpayer.getEstadoUsuario();
        boolean stateAllowsLogin = "ACTIVO".equalsIgnoreCase(accountState)
                || "SANCIONADO".equalsIgnoreCase(accountState);
        boolean sanctioned = "SANCIONADO".equalsIgnoreCase(accountState)
                || Boolean.TRUE.equals(taxpayer.getEstaSancionado());
        if (!stateAllowsLogin || (!Boolean.TRUE.equals(taxpayer.getEstaActivo()) && !sanctioned)) {
            throw new AuthenticationFailure("ACCOUNT_DISABLED",
                    "La cuenta está inactiva, bloqueada o pendiente de verificación. Contacte a administración.");
        }

        boolean representative = taxpayer.getOrganizacionesRepresentadas() != null
                && !taxpayer.getOrganizacionesRepresentadas().isEmpty();
        ArrayNode menu = taxpayerMenus.forTaxpayer(taxpayerFeatures(representative));
        String token = tokenService.create(taxpayer.getUuid(), "TAXPAYER", false,
                permissionCatalog.fromMenu(menu, applicationCode, applicationName));
        return new AuthenticationResponse(token, taxpayer, menu, null, false, "TAXPAYER",
                configuracionSistemaService.obtenerConfiguracionesParaCliente(representative));
    }

    /**
     * Configuration is resolved once into taxpayer permissions at login. The
     * returned normalized menu and the signed token then carry the same
     * authority; request handlers only add ownership/complex validation.
     */
    Set<String> taxpayerFeatures(boolean representative) {
        Set<String> features = new HashSet<>();

        // Organization reservation actions are granted only to representatives.
        // Direct creation takes precedence over the request-only alternative.
        boolean organizationDirect = representative
                && configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_ORGANIZACIONES", true);
        boolean organizationRequest = representative && !organizationDirect
                && configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_ORGANIZACIONES", true);
        if (organizationDirect) {
            features.add("ORGANIZATION_DIRECT");
        } else if (organizationRequest) {
            features.add("ORGANIZATION_REQUEST");
        }
        if (organizationDirect || organizationRequest) {
            // The organization creation action also enables the normal
            // taxpayer lifecycle actions for that representative's own rows.
            features.add("RECURRENT_MANAGEMENT");
        }

        // Each capability resolves independently: a direct flag enables the
        // direct action; otherwise the request action is used when its own
        // flag is enabled. No capability borrows another one's flag.
        addDirectOrRequest(features,
                configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_RECURRENTES", representative),
                configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_RECURRENTES", representative),
                "RECURRENT_CREATE", "RECURRENT_REQUEST", "RECURRENT_MANAGEMENT");

        // Every taxpayer can create an ordinary one-time reservation
        // (por hora / por persona), and can manage only records they own.
        // Organization and event actions are additional signed permissions.
        features.add("PUNTUAL_MANAGEMENT");

        addDirectOrRequest(features,
                configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_RESERVAS_EVENTOS", representative),
                configuracionResolver.permisoHabilitadoParaAudiencia("HABILITAR_SOLICITUD_RESERVAS_EVENTOS", representative),
                "EVENT_CREATE", "EVENT_REQUEST", "PUNTUAL_MANAGEMENT");

        addManagementFeature(features, representative,
                "HABILITAR_RESERVAS_PERIODO_ESCUELAS", "GESTION_MENSUALIDADES");
        addManagementFeature(features, representative,
                "HABILITAR_RESERVAS_BORDEROUX", "GESTION_BORDEROUX");
        if (representative && configuracionResolver.permisoHabilitadoParaAudiencia(
                "HABILITAR_GESTION_ESTUDIANTES", true)) {
            features.add("GESTION_ESTUDIANTES");
        }
        return features;
    }

    private void addDirectOrRequest(Set<String> features, boolean directEnabled, boolean requestEnabled,
                                    String directFeature, String requestFeature, String managementFeature) {
        if (directEnabled) {
            features.add(directFeature);
            features.add(managementFeature);
        } else if (requestEnabled) {
            features.add(requestFeature);
            features.add(managementFeature);
        }
    }

    private void addManagementFeature(Set<String> features, boolean representative,
                                      String configurationKey, String feature) {
        if (configuracionResolver.permisoHabilitadoParaAudiencia(configurationKey, representative)) {
            features.add(feature);
        }
    }

    private JsonNode unwrap(JsonNode response) {
        return response != null && response.has("payload") ? response.get("payload") : response;
    }
    private boolean containsText(JsonNode values, String expected) {
        if (!values.isArray()) return false;
        for (JsonNode value : values) if (expected.equals(value.asText())) return true;
        return false;
    }

    private boolean isCredentialRejection(org.springframework.web.client.HttpStatusCodeException response) {
        if (response.getRawStatusCode() == 401) return true;
        try {
            JsonNode body = objectMapper.readTree(response.getResponseBodyAsByteArray());
            return "bad credentials".equalsIgnoreCase(body.path("message").asText().trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean containsMenu(JsonNode node, String expected) {
        if (node.isObject() && expected.equals(node.path("nombreMenu").asText())) return true;
        if (node.isContainerNode()) {
            for (JsonNode child : node) if (containsMenu(child, expected)) return true;
        }
        return false;
    }

    private static class ExternalCredentialsRejected extends RuntimeException {
        private final UpstreamResponseException upstreamResponse;

        private ExternalCredentialsRejected(UpstreamResponseException upstreamResponse) {
            this.upstreamResponse = upstreamResponse;
        }
    }
    private static class ExternalUnavailable extends RuntimeException {}

    public static class UpstreamResponseException extends RuntimeException {
        private final org.springframework.web.client.HttpStatusCodeException response;

        private UpstreamResponseException(org.springframework.web.client.HttpStatusCodeException response) {
            super("Usuarios returned an HTTP response");
            this.response = response;
        }

        public org.springframework.web.client.HttpStatusCodeException getResponse() {
            return response;
        }
    }

    public static class AuthenticationFailure extends RuntimeException {
        private final String code;
        public AuthenticationFailure(String code, String message) { super(message); this.code = code; }
        public String getCode() { return code; }
    }
}
