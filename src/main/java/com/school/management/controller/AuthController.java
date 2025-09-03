package com.school.management.controller;

import com.school.management.dto.auth.LoginRequest;
import com.school.management.dto.auth.LoginResponse;
import com.school.management.dto.auth.RegisterRequest;
import com.school.management.service.AuthentificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")  // <-- PAS de /api ici, car context-path = /api
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
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
        } catch (Exception e) {
            log.error("Erreur inscription", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erreur serveur: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Connexion : {}", request.getEmail());
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "timestamp", System.currentTimeMillis()
        ));
    }
}
