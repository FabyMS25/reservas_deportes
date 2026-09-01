package com.gamq.deportes.security;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class DeportesPrincipal {
    private final String subject;
    private final String userType;
    private final boolean admin;
}
