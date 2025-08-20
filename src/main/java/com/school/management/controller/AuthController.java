// package com.school.management.controller;

// import com.school.management.dto.auth.LoginRequest;
// import com.school.management.dto.auth.LoginResponse;
// import com.school.management.dto.auth.RegisterRequest;
// import com.school.management.service.AuthentificationService;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.Map;

// @RestController
// @RequestMapping("/auth")
// @RequiredArgsConstructor
// @CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
// @Slf4j
// public class AuthController {

//     // private final AuthenticationSecurity authenticationSecurity;
//     private final AuthentificationService authService;

//     /**
//      * Enregistrement d'un nouvel utilisateur.
//      * @param request DTO contenant les infos d'inscription.
//      * @return JWT token du nouvel utilisateur.
//      */
//     @PostMapping("/register")
//     public ResponseEntity<Map<String, String>> register(
//             @Valid @RequestBody RegisterRequest request) {
//         log.info("Inscription : {} {}", request.getFirstName(), request.getLastName());
//         authService.register(request);
//         return ResponseEntity.status(HttpStatus.CREATED)
//         .body(Map.of("message", "Inscription reussie. veuillez vous connecter."));
//         // AuthentificationResponse response = authenticationSecurity.register(request);
//         // // Renvoie 201 Created pour une création de ressource
//         // return ResponseEntity.status(HttpStatus.CREATED).body(response);
//     }

//     /**
//      * Authentification (login) d'un utilisateur.
//      * @param request DTO contenant email et mot de passe.
//      * @return LoginResponse avec token et infos utilisateur.
//      */
//     @PostMapping("/login")
//     public ResponseEntity<LoginResponse> login(
//             @Valid @RequestBody LoginRequest request) {
//         log.info("Connexion : {}", request.getEmail());
//         return ResponseEntity.ok(authService.login(request));
//         // LoginResponse response = authenticationService.login(request);
//         // return ResponseEntity.ok(response);
//     }

//     /**
//      * Déconnexion : invalide le contexte Spring Security.
//      */
//     @PostMapping("/logout")
//     public ResponseEntity<Void> logout() {
//         authService.logout();
//         return ResponseEntity.noContent().build();
//     }

//     /**
//      * Vérifie si un nom d'utilisateur est disponible.
//      */
//     // @GetMapping("/check-username/{username}")
//     // public ResponseEntity<Map<String, Boolean>> checkUsername(
//     //         @PathVariable String username) {
//     //     boolean exists = authService.usernameExists(username);
//     //     return ResponseEntity.ok(Map.of("available", !exists));
//     // }

//     /**
//      * Vérifie la santé de l'API.
//      */
//     @GetMapping("/health")
//     public ResponseEntity<Map<String, Object>> health() {
//         return ResponseEntity.ok(Map.of(
//             "status", "OK",
//             "timestamp", System.currentTimeMillis()
//         ));
//     }

//     /**
//      * Gestion d'erreur générique.
//      */
//     @RequestMapping("/error")
//     public ResponseEntity<String> handleError() {
//         return ResponseEntity
//                 .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                 .body("Une erreur s'est produite. Veuillez contacter l'administrateur.");
//     }

//     /**
//      * Endpoint de test.
//      */
//     @PostMapping("/test")
//     public ResponseEntity<Map<String, String>> test(
//             @RequestBody Map<String, Object> testData) {
//         log.info("=== TEST ENDPOINT === Données : {}", testData);
//         return ResponseEntity.ok(Map.of(
//             "status", "OK",
//             "message", "Test endpoint fonctionne",
//             "receivedData", testData.toString()
//         ));
//     }
// }


// // package com.school.management.controller;

// // import com.school.management.dto.AuthenticationResponse;
// // import com.school.management.dto.auth.*;
// // import com.school.management.security.AuthenticationSecurity;
// // import com.school.management.service.AuthenticationService;
// // import lombok.RequiredArgsConstructor;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.web.bind.annotation.*;

// // import jakarta.validation.Valid;
// // import java.util.Map;

// // @RestController
// // @RequestMapping("/api/auth")
// // @RequiredArgsConstructor
// // @CrossOrigin(origins = {"http://localhost:4200","http://127.0.0.1:4200"}, allowCredentials = "true")
// // public class AuthController {

// //     private final AuthenticationSecurity authSec;
// //     private final AuthenticationService authSvc;

// //     @PostMapping("/register")
// //     public ResponseEntity<AuthenticationResponse> register(
// //             @Valid @RequestBody RegisterRequest req
// //     ) {
// //         return ResponseEntity.ok(authSec.register(req));
// //     }

// //     @PostMapping("/login")
// //     public ResponseEntity<LoginResponse> login(
// //             @Valid @RequestBody LoginRequest req
// //     ) {
// //         return ResponseEntity.ok(authSvc.login(req));
// //     }

// //     @PostMapping("/logout")
// //     public ResponseEntity<Void> logout() {
// //         authSvc.logout();
// //         return ResponseEntity.noContent().build();
// //     }

// //     @GetMapping("/check-username/{username}")
// //     public ResponseEntity<Map<String, Boolean>> checkUsername(
// //             @PathVariable String username
// //     ) {
// //         boolean exists = authSec.usernameExists(username);
// //         return ResponseEntity.ok(Map.of("available", !exists));
// //     }

// //     @GetMapping("/health")
// //     public ResponseEntity<Map<String,Object>> health() {
// //         return ResponseEntity.ok(Map.of(
// //             "status","OK",
// //             "timestamp",System.currentTimeMillis()
// //         ));
// //     }
// // }


// // src/main/java/com/school/management/controller/AuthController.java
// // package com.school.management.controller;

// // import com.school.management.dto.auth.LoginRequest;
// // import com.school.management.dto.auth.LoginResponse;
// // import com.school.management.dto.auth.RegisterRequest;
// // import com.school.management.service.AuthenticationService;
// // import jakarta.validation.Valid;
// // import lombok.RequiredArgsConstructor;
// // import org.springframework.http.ResponseEntity;
// // import org.springframework.web.bind.annotation.*;

// // @RestController
// // @RequestMapping("/api/auth")
// // @RequiredArgsConstructor
// // @CrossOrigin(origins = "*")
// // // @CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
// // public class AuthController {

    
// //     private final AuthenticationService authService;

// //     @PostMapping("/register")
// //     public ResponseEntity<LoginResponse> register(
// //         @Valid @RequestBody RegisterRequest req
// //     ) {
// //         return ResponseEntity
// //             .status(201)
// //             .body(authService.register(req));
// //     }

// //     @PostMapping("/login")
// //     public ResponseEntity<LoginResponse> login(
// //         @Valid @RequestBody LoginRequest req
// //     ) {
// //         return ResponseEntity.ok(authService.login(req));
// //     }

// //     @PostMapping("/logout")
// //     public ResponseEntity<Void> logout() {
// //         authService.logout();
// //         return ResponseEntity.noContent().build();
// //     }
// // }


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
