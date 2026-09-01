package com.gamq.deportes.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.gamq.deportes.security.DeportesPrincipal;
import com.gamq.deportes.service.UsuariosSessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/usuarios")
public class UsuariosGatewayController {
    private final RestTemplate restTemplate;
    private final UsuariosSessionService sessions;
    private final String usersApiUrl;

    public UsuariosGatewayController(RestTemplate restTemplate, UsuariosSessionService sessions,
            @Value("${usuarios.base-url}") String usersApiUrl) {
        this.restTemplate = restTemplate;
        this.sessions = sessions;
        this.usersApiUrl = usersApiUrl;
    }

    @GetMapping
    public JsonNode list(Authentication authentication) {
        return exchange(authentication, "/listar/usuarios", HttpMethod.GET, null);
    }
    @GetMapping("/{uuid}")
    public JsonNode get(@PathVariable String uuid, Authentication authentication) {
        return exchange(authentication, "/usuario/" + uuid, HttpMethod.GET, null);
    }
    @PutMapping("/user")
    public JsonNode updateUser(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/user/update", HttpMethod.PUT, body);
    }
    @PostMapping("/change-password")
    public JsonNode changePassword(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/user/change-password", HttpMethod.POST, body);
    }
    @PutMapping("/person")
    public JsonNode updatePerson(@RequestBody JsonNode body, Authentication authentication) {
        return exchange(authentication, "/persona", HttpMethod.PUT, body);
    }

    private JsonNode exchange(Authentication authentication, String path, HttpMethod method, JsonNode body) {
        DeportesPrincipal principal = (DeportesPrincipal) authentication.getPrincipal();
        if (!"STAFF".equals(principal.getUserType())) throw new org.springframework.security.access.AccessDeniedException("Staff only");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(sessions.require(principal.getSubject()));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(usersApiUrl + path, method, new HttpEntity<>(body, headers), JsonNode.class).getBody();
    }
}
