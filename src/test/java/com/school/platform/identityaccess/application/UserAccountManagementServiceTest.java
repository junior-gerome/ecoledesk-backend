package com.school.platform.identityaccess.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.shared.domain.exception.BusinessException;

class UserAccountManagementServiceTest {
    private final UserAccountRepository userAccountRepository = Mockito.mock(UserAccountRepository.class);
    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final PasswordResetDeliveryService deliveryService = Mockito.mock(PasswordResetDeliveryService.class);
    private final PasswordResetRateLimitService rateLimitService = Mockito.mock(PasswordResetRateLimitService.class);
    private final UserAccountManagementService service = new UserAccountManagementService(userAccountRepository, roleRepository, passwordEncoder, deliveryService, rateLimitService);

    @Test
    void registerAccountRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ada"); request.setLastName("Lovelace"); request.setEmail("ada@example.com");
        request.setPassword("Password1!"); request.setRoleCode("PARENT");
        when(userAccountRepository.existsByUsername("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerAccount(request))
                .isInstanceOf(BusinessException.class).hasMessageContaining("Email deja utilise");

        verifyNoInteractions(roleRepository, passwordEncoder, rateLimitService);
    }
}

