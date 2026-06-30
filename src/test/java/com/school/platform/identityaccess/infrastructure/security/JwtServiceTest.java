package com.school.platform.identityaccess.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import com.school.platform.identityaccess.infrastructure.security.JwtService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private static final String DEV_FALLBACK_SECRET = "dev-only-local-jwt-secret-change-me-32chars-minimum";
    private static final String STRONG_SECRET = "test-jwt-secret-with-at-least-32-bytes";

    @Mock
    private Environment environment;

    @Test
    @DisplayName("refuse une cle JWT trop courte")
    void shouldRejectSecret_whenSecretIsTooShort() {
        JwtService jwtService = new JwtService(environment);
        ReflectionTestUtils.setField(jwtService, "secretKey", "too-short");

        assertThatThrownBy(jwtService::validateSecretKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 octets");
    }

    @Test
    @DisplayName("refuse la cle de developpement en production")
    void shouldRejectDevFallbackSecret_whenProdProfileIsActive() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"prod"});

        JwtService jwtService = new JwtService(environment);
        ReflectionTestUtils.setField(jwtService, "secretKey", DEV_FALLBACK_SECRET);

        assertThatThrownBy(jwtService::validateSecretKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("developpement");
    }

    @Test
    @DisplayName("accepte une cle forte hors production")
    void shouldAcceptStrongSecret_whenProfileIsNotProd() {
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

        JwtService jwtService = new JwtService(environment);
        ReflectionTestUtils.setField(jwtService, "secretKey", STRONG_SECRET);

        assertThatCode(jwtService::validateSecretKey).doesNotThrowAnyException();
    }
}
