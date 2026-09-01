package com.gamq.deportes.dto.auth;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private final String token;
    private final Object user;
    private final Object menu;
    private final Object staff;
    private final boolean admin;
    private final String userType;
}
