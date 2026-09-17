package com.gamq.deportes.dto.auth;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
@Getter
@RequiredArgsConstructor
public class AuthenticationResponse {
    private final String token;
    private final Object user;
    private final Object menu;
    private final Object staff;
    private final boolean admin;
    private final String userType;
    @lombok.Setter private boolean ruatStatus;
    @lombok.Setter private String ruatMessage;
}
