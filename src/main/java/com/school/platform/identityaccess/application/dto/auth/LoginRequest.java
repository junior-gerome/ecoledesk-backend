package com.school.platform.identityaccess.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "L'email est requis")
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email; // Identifiant de connexion
    
    // @NotBlank(message = "Le mot de passe est requis")
    // @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    // private String password;
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 255, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.\\-_@$!%*?&#])[A-Za-z\\d.\\-_@$!%*?&#]{8,}$",
    message = "Le mot de passe doit contenir au moins : 1 majuscule, 1 minuscule, 1 chiffre et 1 caractère spécial (.-_@$!%*?&#)"
    )
    private String password;

}