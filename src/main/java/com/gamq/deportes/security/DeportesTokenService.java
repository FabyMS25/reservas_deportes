package com.gamq.deportes.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DeportesTokenService {
    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public DeportesTokenService(ObjectMapper objectMapper,
            @Value("${deportes.security.jwt-secret}") String secret,
            @Value("${deportes.security.token-expiration-seconds:28800}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
        if (this.secret.length < 32) throw new IllegalStateException("DEPORTES_JWT_SECRET must have at least 32 characters");
    }

    public String create(String subject, String userType, boolean admin) {
        long now = Instant.now().getEpochSecond();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", subject);
        payload.put("userType", userType);
        payload.put("admin", admin);
        payload.put("iat", now);
        payload.put("exp", now + expirationSeconds);
        try {
            String header = encode(objectMapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
            String body = encode(objectMapper.writeValueAsBytes(payload));
            String unsigned = header + "." + body;
            return unsigned + "." + encode(sign(unsigned));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create the Deportes token", ex);
        }
    }

    public DeportesPrincipal verify(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("Invalid token");
            String unsigned = parts[0] + "." + parts[1];
            if (!java.security.MessageDigest.isEqual(sign(unsigned), Base64.getUrlDecoder().decode(parts[2])))
                throw new IllegalArgumentException("Invalid signature");
            Map<String, Object> claims = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {});
            if (((Number) claims.get("exp")).longValue() <= Instant.now().getEpochSecond())
                throw new IllegalArgumentException("Expired token");
            return new DeportesPrincipal(String.valueOf(claims.get("sub")),
                    String.valueOf(claims.get("userType")), Boolean.TRUE.equals(claims.get("admin")));
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid or expired Deportes token", ex);
        }
    }

    private byte[] sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
    }
    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
