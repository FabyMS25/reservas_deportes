package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamq.deportes.security.DeportesPrincipal;
import com.gamq.deportes.service.UsuariosSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/usuarios")
public class UsuariosGatewayController {
    private final RestTemplate restTemplate;
    private final UsuariosSessionService sessions;
    private final String usersApiUrl;

    public UsuariosGatewayController(RestTemplate restTemplate, UsuariosSessionService sessions,
            @Value("${usuarios.gateway.base-url}") String usersApiUrl) {
        this.restTemplate = restTemplate;
        this.sessions = sessions;
        this.usersApiUrl = usersApiUrl;
    }

    @GetMapping
    @PreAuthorize("@permissions.has('/admin/employees','VER')")
    public JsonNode list(Authentication authentication) {
        return exchange(authentication, "/listar/usuarios", HttpMethod.GET, null);
    }
    @GetMapping("/{uuid}")
    @PreAuthorize("@permissions.staff() and (@permissions.self(#p0) or @permissions.has('/admin/employees','VER'))")
    public JsonNode get(@PathVariable String uuid, Authentication authentication) {
        return exchange(authentication, "/usuario/" + uuid, HttpMethod.GET, null);
    }
    @PutMapping("/user")
    @PreAuthorize("@permissions.has('/admin/employees','ACTUALIZAR')")
    public JsonNode updateUser(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/user/update", HttpMethod.PUT, body);
    }
    @PostMapping("/change-password")
    @PreAuthorize("#p0 != null and @permissions.staff() and @permissions.self(#p0.path('uuidUsuario').asText())")
    public JsonNode changePassword(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/user/change-password", HttpMethod.POST, body);
    }
    @PutMapping("/person")
    @PreAuthorize("#p0 != null and @permissions.staff() and @usuariosSessionService.ownsPerson(authentication.principal.subject,#p0.path('uuid').asText())")
    public JsonNode updatePerson(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/persona", HttpMethod.PUT, body);
    }

    private JsonNode exchange(Authentication authentication, String path, HttpMethod method, JsonNode body) {
        DeportesPrincipal principal = (DeportesPrincipal) authentication.getPrincipal();
        if (!"STAFF".equals(principal.getUserType())) throw new org.springframework.security.access.AccessDeniedException("Staff only");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sessions.require(principal.getSubject()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        try {
            return restTemplate.exchange(usersApiUrl + path, method,
                    new HttpEntity<>(body, headers), JsonNode.class).getBody();
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized ex) {
            sessions.remove(principal.getSubject());
            throw new UsuariosSessionService.SessionExpiredException();
        }
    }
}
