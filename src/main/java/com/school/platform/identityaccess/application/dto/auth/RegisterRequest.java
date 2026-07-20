package com.school.platform.identityaccess.application.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Le prenom est obligatoire")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50)
    private String lastName;

    @Email(message = "L'email doit etre valide")
    @NotBlank(message = "L'email est obligatoire")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 255)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[.\\-_@$!%*?&#])[A-Za-z\\d.\\-_@$!%*?&#]{8,}$")
    private String password;

    @JsonAlias("roleType")
    private String roleCode;
    private Long roleId;
    private Long referenceId;

    @JsonSetter
    public void setReferenceId(String referenceId) {
        if (referenceId == null || referenceId.isEmpty()) this.referenceId = null;
        else try { this.referenceId = Long.parseLong(referenceId); }
        catch (NumberFormatException exception) { throw new ValidationException("referenceId doit etre un nombre"); }
    }
}
