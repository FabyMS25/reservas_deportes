package com.gamq.deportes.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DeportesPermissionsTokenTest {
    private final DeportesTokenService service = new DeportesTokenService(new ObjectMapper(),
            "test-only-secret-at-least-thirty-two-characters", 28800);

    @Test void sameIssuerCarriesTrustedPermissionsForBothUserTypes() {
        for (String type : Set.of("STAFF", "TAXPAYER")) {
            DeportesPrincipal p = service.verify(service.create("user", type, false, Set.of("sch:v")));
            assertEquals(type, p.getUserType());
            assertEquals(Set.of("sch:v"), p.getPermissions());
            assertThrows(UnsupportedOperationException.class, () -> p.getPermissions().add("sch:c"));
        }
    }
    @Test void changingPermissionsWithoutTheSigningKeyIsRejected() {
        String token = service.create("user", "TAXPAYER", false, Set.of("sch:v"));
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("sch:v", "sch:c");
        String forged = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];
        assertThrows(IllegalArgumentException.class, () -> service.verify(forged));
    }
    @Test void expiredTokenCannotCarryUsablePermissions() {
        DeportesTokenService expired = new DeportesTokenService(new ObjectMapper(),
                "test-only-secret-at-least-thirty-two-characters", -1);
        assertThrows(IllegalArgumentException.class, () -> expired.verify(expired.create("user", "STAFF", true, Set.of("cat:c"))));
    }
}
