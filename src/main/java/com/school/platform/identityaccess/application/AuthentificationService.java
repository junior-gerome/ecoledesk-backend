package com.school.platform.identityaccess.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.identityaccess.application.dto.auth.LoginRequest;
import com.school.platform.identityaccess.application.dto.auth.LoginResponse;
import com.school.platform.identityaccess.application.dto.auth.RefreshTokenRequest;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.application.interfaces.IAuthenticationAuditService;
import com.school.platform.identityaccess.application.interfaces.IUserAccountManagementService;
import com.school.platform.identityaccess.domain.model.RefreshToken;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.RefreshTokenRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.identityaccess.infrastructure.security.JwtService;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthentificationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final IAuthenticationAuditService authenticationAuditService;
    private final IUserAccountManagementService usersService;

    @Transactional
    public LoginResponse login(LoginRequest request, String clientIp) {
        loginAttemptService.assertAllowed(request.getEmail(), clientIp);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserAccount account = accountByUsername(request.getEmail());
            account.setLastLogin(LocalDateTime.now());
            String refreshToken = createPersistedRefreshToken(account);
            loginAttemptService.reset(request.getEmail(), clientIp);
            authenticationAuditService.recordLoginSuccess(account, clientIp);
            return response(account, jwtService.generateToken(account), refreshToken);
        } catch (BadCredentialsException exception) {
            loginAttemptService.registerFailure(request.getEmail(), clientIp);
            authenticationAuditService.recordLoginFailure(request.getEmail(), clientIp, "Bad credentials");
            throw exception;
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) { return refresh(request, null); }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request, String clientIp) {
        String username = jwtService.extractUsername(request.refreshToken());
        UserAccount account = accountByUsername(username);
        if (!jwtService.isRefreshTokenValid(request.refreshToken(), account)) {
            authenticationAuditService.recordRefreshRejected(account.getId(), username, clientIp, "Invalid refresh token");
            throw new BusinessException("Refresh token invalide");
        }
        RefreshToken current = refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .orElseThrow(() -> new BusinessException("Refresh token inconnu ou revoque"));
        if (!current.getUser().getId().equals(account.getId())) throw new BusinessException("Refresh token invalide");
        if (!current.isActive(LocalDateTime.now())) handleInactiveRefreshToken(account, current, clientIp);

        String next = jwtService.generateRefreshToken(account);
        String nextHash = hashToken(next);
        current.revoke(nextHash);
        refreshTokenRepository.save(current);
        refreshTokenRepository.save(buildRefreshToken(account, nextHash));
        authenticationAuditService.recordRefreshSuccess(account, clientIp);
        return response(account, jwtService.generateToken(account), next);
    }

    @Transactional
    public void register(RegisterRequest request) { usersService.createAccount(request, true); }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                    .filter(token -> token.getRevokedAt() == null)
                    .ifPresent(token -> { token.revoke(null); refreshTokenRepository.save(token); });
        }
        SecurityContextHolder.clearContext();
    }

    private UserAccount accountByUsername(String username) {
        return userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
    }

    private void handleInactiveRefreshToken(UserAccount account, RefreshToken token, String clientIp) {
        if (token.getRevokedAt() != null && token.getReplacedByTokenHash() != null) {
            refreshTokenRepository.revokeActiveTokensForUser(account.getId(), LocalDateTime.now());
            authenticationAuditService.recordRefreshReuse(account, clientIp);
            throw new BusinessException("Refresh token reutilise; les sessions actives ont ete revoquees");
        }
        authenticationAuditService.recordRefreshRejected(account.getId(), account.getUsername(), clientIp, "Expired or revoked refresh token");
        throw new BusinessException("Refresh token expire ou revoque");
    }

    private LoginResponse response(UserAccount account, String token, String refreshToken) {
        return LoginResponse.builder()
                .token(token).refreshToken(refreshToken).userId(account.getId())
                .nameUser(account.getPerson().getFirstName() + " " + account.getPerson().getLastName())
                .email(account.getUsername()).userProfile(profile(account)).build();
    }

    private Map<String, Object> profile(UserAccount account) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", account.getId());
        profile.put("personId", account.getPerson().getId());
        profile.put("firstName", account.getPerson().getFirstName());
        profile.put("lastName", account.getPerson().getLastName());
        Role role = account.getRoles().stream().findFirst().orElse(null);
        if (role != null) {
            profile.put("roleId", role.getId());
            profile.put("roleCode", role.getCode());
            profile.put("roleLabel", role.getLabel());
        }
        return profile;
    }

    private String createPersistedRefreshToken(UserAccount account) {
        String refreshToken = jwtService.generateRefreshToken(account);
        refreshTokenRepository.save(buildRefreshToken(account, hashToken(refreshToken)));
        return refreshToken;
    }

    private RefreshToken buildRefreshToken(UserAccount account, String tokenHash) {
        RefreshToken token = new RefreshToken();
        token.setUser(account);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpiration() / 1000));
        return token;
    }

    private String hashToken(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException("SHA-256 indisponible", exception); }
    }
}
