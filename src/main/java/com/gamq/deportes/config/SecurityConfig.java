package com.gamq.deportes.config;

import com.gamq.deportes.security.DeportesTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity(prePostEnabled = true) // Changed from @EnableGlobalMethodSecurity (deprecated)
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, DeportesTokenFilter tokenFilter) throws Exception {
        http.httpBasic(it -> it.disable());
        http.formLogin(it -> it.disable());
        http.requestCache(it -> it.disable());
        http.cors(it -> it.disable());
        http.csrf(it -> it.disable());

        http.sessionManagement(it -> it.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(it -> it.authenticationEntryPoint((request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"La sesión ha expirado o no es válida\"}");
        }));

        http.authorizeHttpRequests(it -> it
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/ws-notificaciones/**").permitAll()
                .requestMatchers("/api/auth/login", "/api/passresets/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/reporte/public/ticket/**").permitAll()
                .requestMatchers(HttpMethod.HEAD, "/api/reporte/public/ticket/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/complejos/public").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contribuyente").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contribuyente/public/exists").permitAll()
                .requestMatchers("/api/ruat/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/configuracionSistema/public/**", "/api/system-configurations/public/**")
                .permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
                .requestMatchers(HttpMethod.POST,
                        "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
                .requestMatchers(HttpMethod.PUT,
                        "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
                .requestMatchers(HttpMethod.PATCH,
                        "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
                .requestMatchers(HttpMethod.DELETE,
                        "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
