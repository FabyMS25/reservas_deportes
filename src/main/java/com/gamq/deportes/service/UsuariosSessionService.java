package com.gamq.deportes.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Base64;

@Service
public class UsuariosSessionService {
    private final ConcurrentMap<String, UserSession> sessionsByUser = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public UsuariosSessionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public long expirationBoundedByUpstreamToken(String token, long localExpirationEpoch) {
        Long upstreamExpiration = readJwtExpiration(token);
        return upstreamExpiration == null ? localExpirationEpoch
                : Math.min(localExpirationEpoch, upstreamExpiration);
    }

    public void put(String userUuid, String token, String personUuid, long expiresAtEpoch) {
        sessionsByUser.put(userUuid, new UserSession(token, personUuid, expiresAtEpoch));
    }

    public boolean ownsPerson(String userUuid, String personUuid) {
        UserSession session = activeSession(userUuid);
        return session != null && personUuid != null && !personUuid.isBlank()
                && personUuid.equals(session.personUuid);
    }

    public String require(String userUuid) {
        UserSession session = activeSession(userUuid);
        if (session == null) throw new SessionExpiredException();
        return session.token;
    }

    public void remove(String userUuid) {
        if (userUuid != null) sessionsByUser.remove(userUuid);
    }

    @Scheduled(fixedDelayString = "${usuarios.session-cleanup-interval-ms}")
    public void removeExpiredSessions() {
        long now = Instant.now().getEpochSecond();
        sessionsByUser.entrySet().removeIf(entry -> entry.getValue().expiresAtEpoch <= now);
    }

    private UserSession activeSession(String userUuid) {
        if (userUuid == null) return null;
        UserSession session = sessionsByUser.get(userUuid);
        if (session != null && session.expiresAtEpoch <= Instant.now().getEpochSecond()) {
            sessionsByUser.remove(userUuid, session);
            return null;
        }
        return session;
    }

    private Long readJwtExpiration(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.");
            if (parts.length != 3) return null;
            JsonNode exp = objectMapper.readTree(Base64.getUrlDecoder().decode(parts[1])).path("exp");
            if (!exp.isNumber()) return null;
            return exp.asLong();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static final class UserSession {
        private final String token;
        private final String personUuid;
        private final long expiresAtEpoch;

        private UserSession(String token, String personUuid, long expiresAtEpoch) {
            this.token = token;
            this.personUuid = personUuid;
            this.expiresAtEpoch = expiresAtEpoch;
        }
    }

    public static class SessionExpiredException extends RuntimeException {
        public SessionExpiredException() {
            super("La sesión del sistema de Usuarios expiró. Inicie sesión nuevamente.");
        }
    }
}
