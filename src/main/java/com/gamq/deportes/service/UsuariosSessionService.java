package com.gamq.deportes.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UsuariosSessionService {
    private final Map<String, String> tokensByUser = new ConcurrentHashMap<>();
    public void put(String userUuid, String token) { tokensByUser.put(userUuid, token); }
    public String require(String userUuid) {
        String token = tokensByUser.get(userUuid);
        if (token == null) throw new IllegalStateException("La sesión con el sistema de Usuarios ya no está disponible");
        return token;
    }
}
