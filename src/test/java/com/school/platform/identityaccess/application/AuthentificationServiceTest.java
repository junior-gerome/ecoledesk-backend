package com.school.platform.identityaccess.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.identityaccess.application.AuthentificationService;
import com.school.platform.identityaccess.application.LoginAttemptService;
import com.school.platform.identityaccess.application.dto.auth.LoginRequest;
import com.school.platform.identityaccess.application.dto.auth.RefreshTokenRequest;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.domain.model.UsersProfil;
import com.school.platform.identityaccess.infrastructure.persistence.RefreshTokenRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;
import com.school.platform.identityaccess.infrastructure.security.JwtService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthentificationServiceTest {
    private final UsersRepository usersRepository = Mockito.mock(UsersRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
    private final RefreshTokenRepository refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
    private final LoginAttemptService loginAttemptService = Mockito.mock(LoginAttemptService.class);
    private final AuthentificationService service =
            new AuthentificationService(usersRepository, passwordEncoder, jwtService, authenticationManager,
                    refreshTokenRepository, loginAttemptService);

    @Test
    void publicRegisterRejectsPrivilegedRole() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Ada");
        request.setLastName("Admin");
        request.setEmail("ada.admin@example.com");
        request.setPassword("Password1!");
        request.setRoleType(RoleType.ADMIN);

        when(usersRepository.existsByUsername(request.getEmail())).thenReturn(false);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("administrateur");

        verifyNoInteractions(passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void refreshRejectsUnknownPersistedToken() {
        Users user = new Users();
        user.setId(7L);
        user.setUsername("ada@example.com");
        user.setPassword("Password1!");
        user.setActif(true);

        UsersProfil profil = new UsersProfil();
        profil.setId(11L);
        profil.setFirstName("Ada");
        profil.setLastName("Lovelace");
        profil.setRoleType(RoleType.AGENT);
        user.setProfils(List.of(profil));

        when(jwtService.extractUsername("refresh-token")).thenReturn(user.getUsername());
        when(usersRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.isRefreshTokenValid("refresh-token", user)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest("refresh-token")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inconnu");
    }

    @Test
    void loginResetsAttemptsAfterSuccess() {
        Users user = new Users();
        user.setId(7L);
        user.setUsername("ada@example.com");
        user.setPassword("Password1!");
        user.setActif(true);

        UsersProfil profil = new UsersProfil();
        profil.setId(11L);
        profil.setFirstName("Ada");
        profil.setLastName("Lovelace");
        profil.setRoleType(RoleType.AGENT);
        user.setProfils(List.of(profil));

        LoginRequest request = new LoginRequest("ada@example.com", "Password1!");

        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(org.springframework.security.core.Authentication.class));
        when(usersRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getRefreshExpiration()).thenReturn(604800000L);

        service.login(request, "127.0.0.1");

        verify(loginAttemptService).assertAllowed(request.getEmail(), "127.0.0.1");
        verify(loginAttemptService).reset(request.getEmail(), "127.0.0.1");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void loginRegistersAttemptOnBadCredentials() {
        LoginRequest request = new LoginRequest("ada@example.com", "WrongPass1!");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> service.login(request, "127.0.0.1"))
                .isInstanceOf(BadCredentialsException.class);

        verify(loginAttemptService).assertAllowed(request.getEmail(), "127.0.0.1");
        verify(loginAttemptService).registerFailure(request.getEmail(), "127.0.0.1");
    }
}
