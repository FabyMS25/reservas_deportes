package com.gamq.deportes.config;

import com.gamq.deportes.audit.AuditorAwareimpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origin-patterns}")
    private String[] allowedOriginPatterns;

    @Bean
    AuditorAware<String> auditorProvider() {
        return new AuditorAwareimpl();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition", "X-Request-ID", "X-Response-Origin")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/ws-notificaciones/**")
                .allowedOriginPatterns(allowedOriginPatterns)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        WebMvcConfigurer.super.addCorsMappings(registry);
    }

}
