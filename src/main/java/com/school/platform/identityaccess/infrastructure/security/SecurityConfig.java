package com.school.platform.identityaccess.infrastructure.security;

import com.school.platform.identityaccess.infrastructure.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsProperties corsProperties;
    private final Environment environment;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuration Spring Security...");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authEx) -> {
                            log.warn("Unauthorized access attempt to: {} from {}",
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
                            log.warn("Access denied to: {} for user: {}",
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
                        }))
                .authorizeHttpRequests(auth -> {
                    log.info("Configuration des regles d'autorisation...");
                    auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/register", "/auth/refresh").permitAll()
                            .requestMatchers(HttpMethod.GET, "/auth/health").permitAll()
                            .requestMatchers("/api/ws", "/api/ws/**", "/ws", "/ws/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/password-reset-request").permitAll()
                            .requestMatchers(HttpMethod.POST, "/users/password-reset").permitAll();

                    if (!isProdProfileActive()) {
                        auth
                                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll();
                    }

                    auth
                            .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                            .requestMatchers("/error", "/api/error").permitAll()
                            .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                            .anyRequest().authenticated();

                    log.info("Regles d'autorisation configurees");
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Spring Security configure avec succes");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configuration CORS...");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configure - Origins: {}", configuration.getAllowedOrigins());
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private boolean isProdProfileActive() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
