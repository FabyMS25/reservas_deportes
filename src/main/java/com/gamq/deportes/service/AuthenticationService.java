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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

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

    public AuthenticationService(RestTemplate restTemplate, ObjectMapper objectMapper,
            ContribuyenteService taxpayerService, EncargadoComplejoService staffAssignmentService,
            DeportesTokenService tokenService, UsuariosSessionService usuariosSessions,
            @Value("${usuarios.base-url}") String usersApiUrl,
            @Value("${deportes.application.code}") String applicationCode,
            @Value("${deportes.application.name}") String applicationName,
            @Value("${deportes.application.admin-menu}") String adminMenu) {
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
    }

    public AuthenticationResponse login(AuthenticationRequest request) {
        if (request.getUsername() == null || request.getPassword() == null)
            throw new AuthenticationFailure("BAD_CREDENTIALS", "Usuario o contraseña incorrectos");
        try {
            return loginStaff(request);
        } catch (ExternalCredentialsRejected ignored) {
            return loginTaxpayer(request);
        } catch (ExternalUnavailable externalUnavailable) {
            // Taxpayer authentication remains available even if Usuarios is temporarily unavailable.
            return loginTaxpayer(request);
        }
    }

    private AuthenticationResponse loginStaff(AuthenticationRequest request) {
        JsonNode externalUser;
        try {
            externalUser = restTemplate.postForObject(usersApiUrl + "/auth/signin",
                    Map.of("username", request.getUsername(), "password", request.getPassword()), JsonNode.class);
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {
            throw new ExternalCredentialsRejected();
        } catch (RestClientException ex) {
            throw new ExternalUnavailable();
        }
        externalUser = unwrap(externalUser);
        if (externalUser == null || !externalUser.hasNonNull("token")) throw new ExternalCredentialsRejected();
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
        } catch (RestClientException ex) {
            throw new AuthenticationFailure("USUARIOS_UNAVAILABLE",
                    "No se pudo consultar los permisos en el sistema de Usuarios");
        }
        if (permissions == null || !permissions.isArray() || permissions.isEmpty())
            throw new AuthenticationFailure("NO_PERMISSIONS", "No tiene permisos para acceder a este módulo");

        boolean admin = containsMenu(permissions, adminMenu);
        EncargadoComplejoDto assignment = admin ? null : staffAssignmentService.findByUserUuid(userUuid);
        if (!admin && (assignment == null || assignment.getComplejoDto() == null))
            throw new AuthenticationFailure("NO_ASSIGNATION", "No tiene asignación como encargado o staff");

        // Never expose the Usuarios token to the browser.
        String usuariosToken = externalUser.path("token").asText();
        usuariosSessions.put(userUuid, usuariosToken);
        ((com.fasterxml.jackson.databind.node.ObjectNode) externalUser).remove("token");
        String token = tokenService.create(userUuid, "STAFF", admin);
        return new AuthenticationResponse(token, externalUser, permissions, assignment, admin, "STAFF");
    }

    private AuthenticationResponse loginTaxpayer(AuthenticationRequest request) {
        try {
            login credentials = new login();
            credentials.setUser(request.getUsername());
            credentials.setPass(request.getPassword());
            ContribuyenteDto taxpayer = taxpayerService.loginContribuyente(credentials);
            String token = tokenService.create(taxpayer.getUuid(), "TAXPAYER", false);
            return new AuthenticationResponse(token, taxpayer, Collections.emptyList(), null, false, "TAXPAYER");
        } catch (Exception ex) {
            throw new AuthenticationFailure("BAD_CREDENTIALS", "Usuario o contraseña incorrectos");
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
    private boolean containsMenu(JsonNode node, String expected) {
        if (node.isObject() && expected.equals(node.path("nombreMenu").asText())) return true;
        if (node.isContainerNode()) {
            for (JsonNode child : node) if (containsMenu(child, expected)) return true;
        }
        return false;
    }

    private static class ExternalCredentialsRejected extends RuntimeException {}
    private static class ExternalUnavailable extends RuntimeException {}

    public static class AuthenticationFailure extends RuntimeException {
        private final String code;
        public AuthenticationFailure(String code, String message) { super(message); this.code = code; }
        public String getCode() { return code; }
    }
}
