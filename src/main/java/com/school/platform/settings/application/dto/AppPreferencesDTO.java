package com.school.platform.settings.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppPreferencesDTO(
        @NotBlank @Size(max = 160) String schoolName,
        @Size(max = 50) String schoolCode,
        @Email @NotBlank @Size(max = 150) String schoolEmail,
        @Size(max = 40) String schoolPhone,
        @Size(max = 200) String schoolWebsite,
        @Size(max = 255) String schoolAddress,
        @NotBlank @Size(max = 10) String language,
        @NotBlank @Size(max = 80) String timezone,
        @NotBlank @Size(max = 10) String currency,
        @NotBlank @Size(max = 20) String dateFormat,
        @NotBlank @Size(max = 5) String timeFormat,
        @NotBlank @Size(max = 20) String theme,
        @NotBlank @Size(max = 20) String startOfWeek,
        @NotNull @Min(5) @Max(100) Integer pageSize,
        boolean notifyEmail,
        boolean notifySms,
        boolean notifyAbsence,
        boolean notifyGrades,
        boolean requireTwoFactor,
        @NotNull @Min(5) @Max(240) Integer sessionTimeoutMinutes,
        @NotNull @Min(6) @Max(32) Integer passwordMinLength
) {
    public static AppPreferencesDTO defaults() {
        return new AppPreferencesDTO(
                "Groupe Scolaire Bilingue",
                "GSB",
                "contact@school.local",
                "",
                "",
                "",
                "fr",
                "Africa/Douala",
                "XAF",
                "DD/MM/YYYY",
                "24h",
                "light",
                "MONDAY",
                25,
                true,
                false,
                true,
                true,
                false,
                30,
                8
        );
    }
}
