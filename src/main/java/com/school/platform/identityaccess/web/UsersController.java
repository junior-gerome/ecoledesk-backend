package com.school.platform.identityaccess.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.identityaccess.application.dto.UsersDTO;
import com.school.platform.identityaccess.application.dto.auth.PasswordResetRequest;
import com.school.platform.identityaccess.application.dto.auth.PasswordResetTokenRequest;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.application.UsersService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<List<UsersDTO>> getAllUsers() {
        List<UsersDTO> users = usersService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or authentication.principal.id == #id")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(usersService.updateStatus(id, payload.get("status")));
    }

    @PutMapping("/{id}/role/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTO> assignRole(
            @PathVariable Long id,
            @PathVariable Long roleId) {
        return ResponseEntity.ok(usersService.assignRole(id, roleId));
    }

    @DeleteMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsersDTO> clearDynamicRole(@PathVariable Long id) {
        return ResponseEntity.ok(usersService.clearDynamicRole(id));
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<Map<String, String>> generatePasswordResetToken(
            @Valid @RequestBody PasswordResetTokenRequest req,
            HttpServletRequest request) {
        usersService.requestPasswordReset(req.getEmail(), request.getRemoteAddr());
        return ResponseEntity.ok(Map.of(
                "message",
                "Si ce compte existe, un lien de reinitialisation a ete envoye."
        ));
    }


    @PostMapping("/password-reset")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody PasswordResetRequest req) {
        usersService.resetPassword(req.getEmail(), req.getResetToken(), req.getNewPassword());
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



// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// public class UserController {

//     @Autowired
//     private UserService userService; // Service to access the database

//     @Autowired
//     private ModelMapper modelMapper;

//     @GetMapping("/users/{id}")
//     public ResponseEntity<?> getUserById(@PathVariable Long id) {
//         try {
//             User user = userService.findById(id);
//             if (user == null) {
//                 return ResponseEntity.notFound().header("Custom-Header", "UserNotFound").build();
//             }

        
//             UserDTO userDto = modelMapper.map(user, UserDTO.class);

//             return ResponseEntity.ok()
//                     .header("Custom-Header", "UserFound")
//                     .body(userDto);
//         } catch (Exception ex) {
//             return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                     .header("Custom-Header", "ErrorOccurred")
//                     .body("User not found");
//         }
//     }
// }
