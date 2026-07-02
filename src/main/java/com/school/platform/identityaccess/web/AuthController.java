package com.school.platform.identityaccess.web;

import com.school.platform.identityaccess.application.dto.auth.LoginRequest;
import com.school.platform.identityaccess.application.dto.auth.LoginResponse;
import com.school.platform.identityaccess.application.dto.auth.RefreshTokenRequest;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.identityaccess.application.AuthentificationService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")  // <-- PAS de /api ici, car context-path = /api
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthentificationService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("Inscription : {} {}", request.getFirstName(), request.getLastName());
            authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Inscription réussie"));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inscription", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur serveur"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = resolveClientIp(httpRequest);
        log.info("Connexion : {} depuis {}", request.getEmail(), clientIp);
        return ResponseEntity.ok(authService.login(request, clientIp));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        authService.logout(request == null ? null : request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "timestamp", System.currentTimeMillis()
        ));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
