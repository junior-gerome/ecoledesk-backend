// package com.school.management.controller;

// import com.school.management.dto.UsersDTO;
// import com.school.management.service.UsersService;
// import lombok.RequiredArgsConstructor;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// import jakarta.validation.Valid;
// import java.util.List;
// import java.util.Map;

// @RestController
// @RequestMapping("/api/users")
// @RequiredArgsConstructor
// @CrossOrigin(origins = "*")
// public class UsersController {

//     private final UsersService usersService;

//     /**
//      * Récupérer tous les utilisateurs
//      */
//     @GetMapping
//     @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
//     public ResponseEntity<List<UsersDTO>> getAllUsers() {
//         try {
//             List<UsersDTO> users = usersService.getAllUsers();
//             return ResponseEntity.ok(users);
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }

//     /**
//      * Récupérer un utilisateur par son ID
//      */
//     @GetMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or authentication.principal.id == #id")
//     public ResponseEntity<UsersDTO> getUserById(@PathVariable Long id) {
//         try {
//             UsersDTO user = usersService.getUserById(id);
//             return ResponseEntity.ok(user);
//         } catch (RuntimeException e) {
//             return ResponseEntity.notFound().build();
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }

//     /**
//      * Créer un nouveau utilisateur
//      */
//     @PostMapping
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<?> createUser(@Valid @RequestBody UsersDTO usersDTO) {
//         try {
//             UsersDTO createdUser = usersService.createUser(usersDTO);
//             return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest()
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la création de l'utilisateur"));
//         }
//     }

//     /**
//      * Mettre à jour un utilisateur
//      */
//     @PutMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
//     public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UsersDTO usersDTO) {
//         try {
//             UsersDTO updatedUser = usersService.updateUser(id, usersDTO);
//             return ResponseEntity.ok(updatedUser);
//         } catch (RuntimeException e) {
//             if (e.getMessage().contains("non trouvé")) {
//                 return ResponseEntity.notFound().build();
//             }
//             return ResponseEntity.badRequest()
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la mise à jour de l'utilisateur"));
//         }
//     }

//     /**
//      * Supprimer (désactiver) un utilisateur
//      */
//     @DeleteMapping("/{id}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
//         try {
//             usersService.deleteUser(id);
//             return ResponseEntity.ok(Map.of("message", "Utilisateur désactivé avec succès"));
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la suppression de l'utilisateur"));
//         }
//     }

//     /**
//      * Mettre à jour la dernière connexion
//      */
//     @PatchMapping("/{id}/last-login")
//     @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
//     public ResponseEntity<Map<String, String>> updateLastLogin(@PathVariable Long id) {
//         try {
//             usersService.updateLastLogin(id);
//             return ResponseEntity.ok(Map.of("message", "Dernière connexion mise à jour"));
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la mise à jour"));
//         }
//     }

//     /**
//      * Générer un token de réinitialisation de mot de passe
//      */
//     @PostMapping("/password-reset-request")
//     public ResponseEntity<Map<String, String>> generatePasswordResetToken(@RequestBody Map<String, String> request) {
//         try {
//             String email = request.get("email");
//             if (email == null || email.trim().isEmpty()) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("error", "Email requis"));
//             }
            
//             String resetToken = usersService.generatePasswordResetToken(email);
//             return ResponseEntity.ok(Map.of(
//                 "message", "Token de réinitialisation généré", 
//                 "resetToken", resetToken
//             ));
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la génération du token"));
//         }
//     }

//     /**
//      * Réinitialiser le mot de passe
//      */
//     @PostMapping("/password-reset")
//     public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request) {
//         try {
//             String email = request.get("email");
//             String resetToken = request.get("resetToken");
//             String newPassword = request.get("newPassword");

//             if (email == null || resetToken == null || newPassword == null) {
//                 return ResponseEntity.badRequest()
//                         .body(Map.of("error", "Email, token et nouveau mot de passe requis"));
//             }

//             usersService.resetPassword(email, resetToken, newPassword);
//             return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès"));
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest()
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors de la réinitialisation du mot de passe"));
//         }
//     }

//     /**
//      * Vérifier si un nom d'utilisateur existe
//      */
//     @GetMapping("/check-username/{username}")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
//         try {
//             // Cette méthode devrait être ajoutée dans le service
//             boolean exists = usersService.existsByUsername(username);
//             return ResponseEntity.ok(Map.of("exists", exists));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//         }
//     }

//     /**
//      * Activer/Désactiver un utilisateur
//      */
//     @PatchMapping("/{id}/toggle-status")
//     @PreAuthorize("hasRole('ADMIN')")
//     public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
//         try {
//             UsersDTO user = usersService.getUserById(id);
//             // Cette logique devrait être dans le service
//             user.setActif(!user.getActif());
//             UsersDTO updatedUser = usersService.updateUser(id, user);
            
//             String status = updatedUser.getActif() ? "activé" : "désactivé";
//             return ResponseEntity.ok(Map.of(
//                 "message", "Utilisateur " + status + " avec succès",
//                 "user", updatedUser
//             ));
//         } catch (RuntimeException e) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .body(Map.of("error", e.getMessage()));
//         } catch (Exception e) {
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(Map.of("error", "Erreur lors du changement de statut"));
//         }
//     }
// }

package com.school.management.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.management.dto.UsersDTO;
import com.school.management.dto.auth.RegisterRequest;
import com.school.management.service.UsersService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class UsersController {

    private final UsersService usersService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UsersDTO>> getAllUsers() {
        List<UsersDTO> users = usersService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or authentication.principal.id == #id")
    public ResponseEntity<UsersDTO> getUserById(@PathVariable Long id) {
        UsersDTO user = usersService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTO> createUser(@Valid @RequestBody RegisterRequest registerRequest) {
        UsersDTO created = usersService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable Long id,
                                               @Valid @RequestBody UsersDTO dto) {
        UsersDTO updated = usersService.updateUser(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/last-login")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id")
    public ResponseEntity<Void> updateLastLogin(@PathVariable Long id) {
        usersService.updateLastLogin(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Map<String, String>> generatePasswordResetToken(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String token = usersService.generatePasswordResetToken(email);
        return ResponseEntity.ok(Map.of("resetToken", token));
    }


    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String resetToken = req.get("resetToken");
        String newPassword = req.get("newPassword");
        usersService.resetPassword(email, resetToken, newPassword);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> checkUsernameExists(@PathVariable String username) {
        boolean exists = usersService.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // @PatchMapping("/{id}/toggle-status")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<Map<String, Object>> toggleUserStatus(@PathVariable Long id) {
    //     UsersDTO user = usersService.toggleUserStatus(id);
    //     return ResponseEntity.ok(Map.of(
    //         "message", "Utilisateur " + (user.getActif() ? "activé" : "désactivé") + " avec succès",
    //         "user", user
    //     ));
    // }
}