package com.school.management.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Chemins qui ne nécessitent AUCUNE vérification JWT
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
    "/api/auth/",
        "/api/students",
        "/users/password-reset-request",
        "/users/password-reset",
        "/swagger-ui/",
        "/v3/api-docs/",
        "/actuator/",
        "/error"
    );

     /**
     * Vérifie si l'endpoint est public (ne nécessite pas d'authentification)
     */
    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_PATHS.stream().anyMatch(requestURI::startsWith);
    }
    
//     private boolean isPublicEndpoint(String requestURI) {
//     return PUBLIC_PATHS.stream().anyMatch(path -> requestURI.matches(path + "(/.*)?"));
// }
    

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        log.debug("🔍 JwtFilter - URI: {} | Method: {}", requestURI, method);

        // 1. Toujours laisser passer les requêtes OPTIONS (CORS preflight)
        if ("OPTIONS".equals(method)) {
            log.debug("✅ OPTIONS request - Bypass JWT filter");
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Vérifier si c'est un endpoint public
        if (isPublicEndpoint(requestURI)) {
            log.debug("✅ Public endpoint detected: {} - Bypass JWT filter", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Pour les endpoints protégés, vérifier le token JWT
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("❌ No Bearer token found for protected endpoint: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("✅ User authenticated: {}", userEmail);
                } else {
                    log.warn("❌ Invalid JWT token for user: {}", userEmail);
                }
            }

        } catch (JwtException e) {
            log.error("❌ JWT Exception: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Unexpected error in JWT filter: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

   

    /**
     * Optionnel: Override pour exclure complètement certains patterns
     */
    @Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI(); // path est du type: /auth/register
    return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
            || "OPTIONS".equalsIgnoreCase(request.getMethod());
}

}