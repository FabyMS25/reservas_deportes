package com.gamq.deportes.audit;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.gamq.deportes.security.DeportesPrincipal;

import java.util.Optional;

public class AuditorAwareimpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DeportesPrincipal) {
            String subject = ((DeportesPrincipal) authentication.getPrincipal()).getSubject();
            if (subject != null && !subject.trim().isEmpty()) {
                return Optional.of(subject.trim());
            }
        }
        return Optional.of("SYSTEM");
    }
}
