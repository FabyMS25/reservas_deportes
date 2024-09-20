package com.gamq.deportes.config;

import com.gamq.deportes.audit.AuditorAwareimpl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;

public class JpaAuditConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareimpl();
    }

}
