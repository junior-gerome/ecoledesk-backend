package com.school.management.dto.auth;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.school.management.enums.RoleType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 50, message = "Le prénom ne peut pas dépasser 50 caractères")
    private String firstName;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String lastName;
    
    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 255, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    @Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.\\-_@$!%*?&#])[A-Za-z\\d.\\-_@$!%*?&#]{8,}$",
    message = "Le mot de passe doit contenir au moins : 1 majuscule, 1 minuscule, 1 chiffre et 1 caractère spécial (.-_@$!%*?&#)"
    )
    private String password;
    
    @NotNull(message = "Le type d'utilisateur est obligatoire")
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    
    private Long referenceId;

    // Transformation String -> Long
    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    // Accepte aussi les strings pour la désérialisation
    @JsonSetter
    public void setReferenceId(String referenceId) {
        if (referenceId == null || referenceId.isEmpty()) {
            this.referenceId = null;
        } else {
            try {
                this.referenceId = Long.parseLong(referenceId);
            } catch (NumberFormatException e) {
                throw new ValidationException("referenceId doit être un nombre");
            }
        }
    }
}