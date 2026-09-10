package com.gamq.deportes.controller;

import org.springframework.security.access.prepost.PreAuthorize;

import com.gamq.deportes.dto.auth.AuthenticationRequest;
import com.gamq.deportes.dto.auth.AuthenticationResponse;
import com.gamq.deportes.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        } catch (AuthenticationService.AuthenticationFailure ex) {
            HttpStatus status = "BAD_CREDENTIALS".equals(ex.getCode())
                    ? HttpStatus.UNAUTHORIZED
                    : "USUARIOS_UNAVAILABLE".equals(ex.getCode())
                        ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).body(Map.of("code", ex.getCode(), "message", ex.getMessage()));
        }
    }
}
