package com.gamq.deportes.config;

import com.gamq.deportes.audit.AuditorAwareimpl;
import com.gamq.deportes.interceptor.HeaderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    @Qualifier("headerInterceptor")
    private HeaderInterceptor headerInterceptor;

    @Bean
    AuditorAware<String> auditorProvider() {
        return new AuditorAwareimpl();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(headerInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/reporte/**","/");

        WebMvcConfigurer.super.addInterceptors(registry);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(
                        "https://deportes.quillacollo.gob.bo",
                        "http://127.0.0.1:4205",
                        "http://181.177.143.187:4205",
                        "http://200.58.81.34:4206",
                        "http://200.58.81.34:4203",
                        "http://localhost:4200", "http://localhost:4205"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);

        registry.addMapping("/ws-notificaciones/**")
                .allowedOriginPatterns(
                        "https://deportes.quillacollo.gob.bo",
                        "http://127.0.0.1:4205",
                        "http://181.177.143.187:4205",
                        "http://localhost:4200", "http://localhost:4205"
                )
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);

        WebMvcConfigurer.super.addCorsMappings(registry);
    }

}
