package com.school.management.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.school.management.security.JwtAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("🔧 Configuration Spring Security...");
        
        http
            // 1) CORS - CRITIQUE : Doit être en premier !
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2) Désactiver CSRF pour les APIs REST
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3) Session stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 4) Gestion des erreurs personnalisées
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, authEx) -> {
                    log.warn("🚫 Unauthorized access attempt to: {} from {}", 
                        req.getRequestURI(), req.getRemoteAddr());
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(String.format("""
                        {
                          "success": false,
                          "error": "Unauthorized",
                          "message": "Authentication required",
                          "status": 401,
                          "path": "%s",
                          "timestamp": %d
                        }
                        """, req.getRequestURI(), System.currentTimeMillis()));
                })
                .accessDeniedHandler((req, res, deniedEx) -> {
                    log.warn("🚫 Access denied to: {} for user: {}", 
                        req.getRequestURI(), req.getUserPrincipal());
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(String.format("""
                        {
                          "success": false,
                          "error": "Forbidden",
                          "message": "Insufficient permissions",
                          "status": 403,
                          "path": "%s",
                          "timestamp": %d
                        }
                        """, req.getRequestURI(), System.currentTimeMillis()));
                })
            )
            
            // 5) Règles d'autorisation - ORDRE CRUCIAL !
            .authorizeHttpRequests(auth -> {
                log.info("📋 Configuration des règles d'autorisation...");
                auth
                    // Preflight CORS - DOIT être en premier
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                      

                    // Endpoints d'authentification - PUBLICS
                    // .requestMatchers("/auth/**").permitAll()
                            // .requestMatchers("/api/students/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/api/students/**").permitAll() // ✅ corrige le problème
                    
                    // Reset password - PUBLICS  
                    .requestMatchers(HttpMethod.POST, "/users/password-reset-request").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users/password-reset").permitAll()
                    
                    // Documentation API - PUBLICS
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                    
                    // Monitoring - PUBLICS
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                    
                    // Pages d'erreur - PUBLIQUES
                    .requestMatchers("/error", "/api/error").permitAll()
                    
                    // Ressources statiques - PUBLIQUES
                    .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                    
                    // TOUT LE RESTE = AUTHENTIFICATION REQUISE
                    .anyRequest().authenticated();
                    
                log.info("✅ Règles d'autorisation configurées");
            })
            
            // 6) Ajouter le filtre JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("✅ Spring Security configuré avec succès");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("🌐 Configuration CORS...");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Origins autorisées - UTILISER setAllowedOrigins pour être explicite
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://127.0.0.1:4200"
        ));
        
        // Méthodes HTTP autorisées
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));
        
        // Headers autorisés
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type", 
            "Accept",
            "Origin",
            "X-Requested-With",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Headers exposés
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization"
        ));
        
        // Credentials autorisés
        configuration.setAllowCredentials(true);
        
        // Cache preflight 1 heure
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("✅ CORS configuré - Origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}