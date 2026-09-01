package com.gamq.deportes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String code;
    private final String message;
    private final Instant timestamp;
    private final String path;

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, Instant.now(), path);
    }
}
