package com.school.management.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    // TODO: Implement JWT token generation and validation logic

    public String generateToken(Authentication authentication) {
        // TODO: Implement token generation logic
        return "token";
    }
}
