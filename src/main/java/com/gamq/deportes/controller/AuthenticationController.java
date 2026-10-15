package com.gamq.deportes.controller;

import com.gamq.deportes.dto.auth.AuthenticationRequest;
import com.gamq.deportes.dto.auth.AuthenticationResponse;
import com.gamq.deportes.service.AuthenticationService;
import com.gamq.deportes.security.DeportesPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@PreAuthorize("denyAll()")
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationService.UpstreamResponseException ex) {
            org.springframework.web.client.HttpStatusCodeException upstream = ex.getResponse();
            HttpHeaders headers = new HttpHeaders();
            HttpHeaders upstreamHeaders = upstream.getResponseHeaders();
            if (upstreamHeaders != null) {
                for (String name : new String[]{HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_ENCODING,
                        HttpHeaders.CONTENT_LANGUAGE, HttpHeaders.RETRY_AFTER, "X-Request-ID"}) {
                    if (upstreamHeaders.containsKey(name)) headers.put(name, upstreamHeaders.get(name));
                }
            }
            return ResponseEntity.status(upstream.getStatusCode()).headers(headers)
                    .body(upstream.getResponseBodyAsByteArray());
        } catch (AuthenticationService.AuthenticationFailure ex) {
            HttpStatus status = "BAD_CREDENTIALS".equals(ex.getCode())
                    ? HttpStatus.UNAUTHORIZED
                    : ("USUARIOS_UNAVAILABLE".equals(ex.getCode())
                        || "CONTRIBUYENTES_UNAVAILABLE".equals(ex.getCode()))
                        ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(Map.of("code", ex.getCode(), "message", ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(Authentication authentication) {
        if (authentication.getPrincipal() instanceof DeportesPrincipal) {
            DeportesPrincipal principal = (DeportesPrincipal) authentication.getPrincipal();
            authenticationService.logout(principal.getSubject(), principal.getUserType());
        }
        return ResponseEntity.noContent().build();
    }
}
