package com.gamq.deportes.security;
import lombok.Getter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
@Getter
public class DeportesPrincipal {
    private final String subject;
    private final String userType;
    private final boolean admin;
    private final Set<String> permissions;

    public DeportesPrincipal(String subject, String userType, boolean admin) {
        this(subject, userType, admin, Collections.emptySet());
    }

    public DeportesPrincipal(String subject, String userType, boolean admin, Set<String> permissions) {
        this.subject = subject;
        this.userType = userType;
        this.admin = admin;
        this.permissions = Collections.unmodifiableSet(new HashSet<>(permissions));
    }
}
