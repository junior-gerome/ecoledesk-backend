package com.school.platform.identityaccess.application.dto.useraccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserAccountCreateRequest {
    @NotNull(message = "La personne est obligatoire")
    @Positive(message = "L'identifiant de la personne doit etre positif")
    private Long personId;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(max = 80, message = "Le nom d'utilisateur ne doit pas depasser 80 caracteres")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 128, message = "Le mot de passe doit contenir entre 8 et 128 caracteres")
    private String password;
}
