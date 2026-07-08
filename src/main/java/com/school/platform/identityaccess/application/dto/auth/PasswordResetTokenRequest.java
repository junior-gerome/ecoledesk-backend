package com.school.platform.identityaccess.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetTokenRequest {
    @Email
    @NotBlank
    private String email;
}
