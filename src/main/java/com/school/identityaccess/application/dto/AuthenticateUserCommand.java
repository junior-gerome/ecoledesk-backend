package com.school.identityaccess.application.dto;

public record AuthenticateUserCommand(String email, String rawPassword) {
}
