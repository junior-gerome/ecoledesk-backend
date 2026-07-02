package com.school.platform.identityaccess.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter = new ExposedJwtAuthenticationFilter(
            Mockito.mock(JwtService.class),
            Mockito.mock(UserDetailsService.class)
    );

    @Test
    void shouldSkipPublicAuthEndpointBehindApiContextPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setContextPath("/api");
        request.setServletPath("/auth/login");

        assertThat(((ExposedJwtAuthenticationFilter) filter).shouldSkip(request)).isTrue();
    }

    @Test
    void shouldNotSkipProtectedEndpointBehindApiContextPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/students");
        request.setContextPath("/api");
        request.setServletPath("/students");

        assertThat(((ExposedJwtAuthenticationFilter) filter).shouldSkip(request)).isFalse();
    }

    private static class ExposedJwtAuthenticationFilter extends JwtAuthenticationFilter {
        ExposedJwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
            super(jwtService, userDetailsService);
        }

        boolean shouldSkip(MockHttpServletRequest request) {
            return shouldNotFilter(request);
        }
    }
}
