package com.school.identityaccess.application.dto;

public record RegisterUserCommand(
        String email,
        String rawPassword,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileCode
) {
    public String effectiveProfileCode() {
        return profileCode == null || profileCode.isBlank() ? "TEACHING_STAFF" : profileCode.trim().toUpperCase();
    }
}
