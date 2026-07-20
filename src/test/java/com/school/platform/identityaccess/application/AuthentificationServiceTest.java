package com.school.platform.identityaccess.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.school.platform.identityaccess.application.dto.auth.LoginRequest;
import com.school.platform.identityaccess.application.dto.auth.RefreshTokenRequest;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.domain.model.RefreshToken;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.RefreshTokenRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.identityaccess.infrastructure.security.JwtService;
import com.school.platform.shared.domain.exception.BusinessException;

class AuthentificationServiceTest {
    private final UserAccountRepository userAccountRepository = Mockito.mock(UserAccountRepository.class);
    private final PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    private final JwtService jwtService = Mockito.mock(JwtService.class);
    private final AuthenticationManager authenticationManager = Mockito.mock(AuthenticationManager.class);
    private final RefreshTokenRepository refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
    private final LoginAttemptService loginAttemptService = Mockito.mock(LoginAttemptService.class);
    private final AuthenticationAuditService auditService = Mockito.mock(AuthenticationAuditService.class);
    private final UserAccountManagementService usersService = Mockito.mock(UserAccountManagementService.class);
    private final AuthentificationService service = new AuthentificationService(userAccountRepository, passwordEncoder, jwtService,
            authenticationManager, refreshTokenRepository, loginAttemptService, auditService, usersService);

    @Test
    void refreshRejectsUnknownPersistedToken() {
        UserAccount account = account("ada@example.com");
        when(jwtService.extractUsername("refresh-token")).thenReturn(account.getUsername());
        when(userAccountRepository.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        when(jwtService.isRefreshTokenValid("refresh-token", account)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest("refresh-token"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("inconnu");
    }

    @Test
    void refreshReuseRevokesActiveUserTokens() {
        UserAccount account = account("ada@example.com");
        RefreshToken token = new RefreshToken();
        token.setUser(account); token.setTokenHash("current-hash"); token.setExpiresAt(LocalDateTime.now().plusMinutes(5)); token.revoke("next-hash");
        when(jwtService.extractUsername("refresh-token")).thenReturn(account.getUsername());
        when(userAccountRepository.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        when(jwtService.isRefreshTokenValid("refresh-token", account)).thenReturn(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.refresh(new RefreshTokenRequest("refresh-token"), "127.0.0.1"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("reutilise");
        verify(refreshTokenRepository).revokeActiveTokensForUser(Mockito.eq(account.getId()), any(LocalDateTime.class));
        verify(auditService).recordRefreshReuse(account, "127.0.0.1");
    }

    @Test
    void loginResetsAttemptsAfterSuccess() {
        UserAccount account = account("ada@example.com");
        LoginRequest request = new LoginRequest("ada@example.com", "Password1!");
        when(authenticationManager.authenticate(any())).thenReturn(Mockito.mock(org.springframework.security.core.Authentication.class));
        when(userAccountRepository.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        when(jwtService.generateToken(account)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(account)).thenReturn("refresh-token");
        when(jwtService.getRefreshExpiration()).thenReturn(604800000L);

        service.login(request, "127.0.0.1");

        verify(loginAttemptService).assertAllowed(request.getEmail(), "127.0.0.1");
        verify(loginAttemptService).reset(request.getEmail(), "127.0.0.1");
        verify(auditService).recordLoginSuccess(account, "127.0.0.1");
    }

    private UserAccount account(String username) {
        Person person = new Person(); person.setFirstName("Ada"); person.setLastName("Lovelace"); person.setEmail(username);
        UserAccount account = new UserAccount(); account.setId(7L); account.setUsername(username); account.setPassword("Password1!"); account.setEnabled(true); account.setPerson(person);
        return account;
    }
}
