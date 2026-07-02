package com.school.platform.identityaccess.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtService jwtService;

    public String generateToken(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication ne doit pas etre null");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return jwtService.generateToken(userDetails);
        }

        UserDetails fallbackUser = User.withUsername(authentication.getName())
                .password("N/A")
                .authorities(authentication.getAuthorities())
                .build();
        return jwtService.generateToken(fallbackUser);
    }
}
