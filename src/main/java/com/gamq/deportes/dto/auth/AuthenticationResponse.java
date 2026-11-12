package com.gamq.deportes.dto.auth;
import com.gamq.deportes.dto.ConfiguracionSistemaDto;
import lombok.RequiredArgsConstructor;
import lombok.Getter;
import java.util.List;
@Getter
@RequiredArgsConstructor
public class AuthenticationResponse {
    private final String token;
    private final Object user;
    private final Object menu;
    private final Object staff;
    private final boolean admin;
    private final String userType;
    /** Safe UI configuration snapshot; excludes RUAT, notifications and permissions. */
    private final List<ConfiguracionSistemaDto> configurations;
    @lombok.Setter private boolean ruatStatus;
    @lombok.Setter private String ruatMessage;
}
