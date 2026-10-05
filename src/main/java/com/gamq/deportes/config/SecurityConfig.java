package com.gamq.deportes.config;

import com.gamq.deportes.security.DeportesTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, DeportesTokenFilter tokenFilter) throws Exception {
        // Browser login is handled by Angular and /api/auth/login, never an HTTP challenge.
        http.httpBasic().disable();
        http.formLogin().disable();
        http.requestCache().disable();
        http.cors().and().csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
                response.setStatus(401);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"La sesión ha expirado o no es válida\"}");
            }).and()
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // SockJS cannot add an Authorization header to its HTTP /info request.
            // Authentication is performed on the subsequent STOMP CONNECT frame.
            .antMatchers("/ws-notificaciones/**").permitAll()
            .antMatchers("/api/auth/login", "/api/passresets/**",
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .antMatchers(HttpMethod.GET, "/api/reporte/public/ticket/**").permitAll()
            .antMatchers(HttpMethod.HEAD, "/api/reporte/public/ticket/**").permitAll()
            .antMatchers(HttpMethod.POST, "/api/contribuyente").permitAll()
            .antMatchers(HttpMethod.GET, "/api/contribuyente/public/exists").permitAll()
            .antMatchers("/api/ruat/**").permitAll()
            .antMatchers(HttpMethod.POST,
                    "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
            .antMatchers(HttpMethod.PUT,
                    "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
            .antMatchers(HttpMethod.PATCH,
                    "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
            .antMatchers(HttpMethod.DELETE,
                    "/api/configuracionSistema/**", "/api/system-configurations/**")
                .hasAuthority("DEPORTES_ADMIN")
            .anyRequest().authenticated().and()
            .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
