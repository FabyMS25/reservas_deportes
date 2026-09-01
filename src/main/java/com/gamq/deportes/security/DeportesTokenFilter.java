package com.gamq.deportes.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Component
public class DeportesTokenFilter extends OncePerRequestFilter {
    private final DeportesTokenService tokenService;
    public DeportesTokenFilter(DeportesTokenService tokenService) { this.tokenService = tokenService; }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                DeportesPrincipal principal = tokenService.verify(authorization.substring(7));
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(
                        "TAXPAYER".equalsIgnoreCase(principal.getUserType())
                                ? "DEPORTES_TAXPAYER"
                                : "DEPORTES_STAFF"));
                if (principal.isAdmin()) {
                    authorities.add(new SimpleGrantedAuthority("DEPORTES_ADMIN"));
                }
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(principal, null, authorities));
            } catch (IllegalArgumentException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
