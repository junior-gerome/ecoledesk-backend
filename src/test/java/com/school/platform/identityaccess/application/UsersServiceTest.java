package com.school.platform.identityaccess.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.identityaccess.application.PasswordResetDeliveryService;
import com.school.platform.identityaccess.application.PasswordResetRateLimitService;
import com.school.platform.identityaccess.application.UsersService;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersProfilRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

class UsersServiceTest {
    private final UsersRepository usersRepository = Mockito.mock(UsersRepository.class);
    private final UsersProfilRepository profilRepository = Mockito.mock(UsersProfilRepository.class);
    private final RoleRepository roleRepository = Mockito.mock(RoleRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final PasswordResetDeliveryService passwordResetDeliveryService = Mockito.mock(PasswordResetDeliveryService.class);
    private final PasswordResetRateLimitService passwordResetRateLimitService = Mockito.mock(PasswordResetRateLimitService.class);
    private final UsersService service = new UsersService(usersRepository, profilRepository, roleRepository, passwordEncoder,
            passwordResetDeliveryService, passwordResetRateLimitService);

    @Test
    void registerUserRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ada");
        request.setLastName("Lovelace");
        request.setEmail("ada@example.com");
        request.setPassword("Password1!");
        request.setRoleType(RoleType.PARENT);

        when(usersRepository.existsByUsername("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registerUser(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Email deja utilise");

        verifyNoInteractions(profilRepository, roleRepository, passwordEncoder, passwordResetRateLimitService);
    }
}