package com.school.platform.identityaccess.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
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
import com.school.platform.identityaccess.domain.model.RefreshToken;
import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.domain.model.UsersProfil;
import com.school.platform.identityaccess.infrastructure.persistence.RefreshTokenRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;
import com.school.platform.identityaccess.infrastructure.security.JwtService;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthentificationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginAttemptService loginAttemptService;
    private final AuthenticationAuditService authenticationAuditService;

    @Transactional
    public LoginResponse login(LoginRequest req, String clientIp) {
        loginAttemptService.assertAllowed(req.getEmail(), clientIp);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);

            Users user = usersRepository.findByUsername(req.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
            user.setLastLogin(LocalDateTime.now());

            String token = jwtService.generateToken(user);
            UsersProfil profil = primaryProfile(user);
            String refreshToken = createPersistedRefreshToken(user);
            loginAttemptService.reset(req.getEmail(), clientIp);
            authenticationAuditService.recordLoginSuccess(user, clientIp);

            return buildLoginResponse(user, profil, token, refreshToken);
        } catch (BadCredentialsException ex) {
            loginAttemptService.registerFailure(req.getEmail(), clientIp);
            authenticationAuditService.recordLoginFailure(req.getEmail(), clientIp, "Bad credentials");
            throw ex;
        }
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        return refresh(request, null);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request, String clientIp) {
        String username = jwtService.extractUsername(request.refreshToken());
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        if (!jwtService.isRefreshTokenValid(request.refreshToken(), user)) {
            authenticationAuditService.recordRefreshRejected(user.getId(), username, clientIp, "Invalid refresh token");
            throw new BusinessException("Refresh token invalide");
        }

        String currentHash = hashToken(request.refreshToken());
        RefreshToken persistedToken = refreshTokenRepository.findByTokenHash(currentHash)
                .orElseThrow(() -> {
                    authenticationAuditService.recordRefreshRejected(user.getId(), username, clientIp,
                            "Unknown refresh token");
                    return new BusinessException("Refresh token inconnu ou revoque");
                });

        LocalDateTime now = LocalDateTime.now();
        if (!persistedToken.getUser().getId().equals(user.getId())) {
            authenticationAuditService.recordRefreshRejected(user.getId(), username, clientIp,
                    "Refresh token user mismatch");
            throw new BusinessException("Refresh token invalide");
        }

        if (!persistedToken.isActive(now)) {
            handleInactiveRefreshToken(user, persistedToken, clientIp);
        }

        UsersProfil profil = primaryProfile(user);
        String nextRefreshToken = jwtService.generateRefreshToken(user);
        String nextHash = hashToken(nextRefreshToken);
        RefreshToken nextPersistedToken = buildRefreshToken(user, nextHash);
        persistedToken.revoke(nextHash);
        refreshTokenRepository.save(persistedToken);
        refreshTokenRepository.save(nextPersistedToken);
        authenticationAuditService.recordRefreshSuccess(user, clientIp);

        return buildLoginResponse(user, profil, jwtService.generateToken(user), nextRefreshToken);
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (usersRepository.existsByUsername(req.getEmail())) {
            throw new BusinessException("Email deja utilise");
        }
        if (!isSelfRegistrableRole(req.getRoleType())) {
            throw new BusinessException("Ce type de profil doit etre cree par un administrateur.");
        }

        Users user = new Users();
        user.setUsername(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setActif(true);

        UsersProfil profil = new UsersProfil();
        profil.setEmailUser(req.getEmail());
        profil.setFirstName(req.getFirstName());
        profil.setLastName(req.getLastName());
        profil.setRoleType(req.getRoleType());
        profil.setReferenceId(req.getReferenceId());
        profil.setUser(user);

        user.setProfils(List.of(profil));
        usersRepository.save(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.findByTokenHash(hashToken(refreshToken))
                    .filter(token -> token.getRevokedAt() == null)
                    .ifPresent(token -> {
                        token.revoke(null);
                        refreshTokenRepository.save(token);
                    });
        }
        SecurityContextHolder.clearContext();
    }

    private void handleInactiveRefreshToken(Users user, RefreshToken persistedToken, String clientIp) {
        if (persistedToken.getRevokedAt() != null && persistedToken.getReplacedByTokenHash() != null) {
            refreshTokenRepository.revokeActiveTokensForUser(user.getId(), LocalDateTime.now());
            authenticationAuditService.recordRefreshReuse(user, clientIp);
            throw new BusinessException("Refresh token reutilise; les sessions actives ont ete revoquees");
        }
        authenticationAuditService.recordRefreshRejected(user.getId(), user.getUsername(), clientIp,
                "Expired or revoked refresh token");
        throw new BusinessException("Refresh token expire ou revoque");
    }

    private LoginResponse buildLoginResponse(Users user, UsersProfil profil, String token, String refreshToken) {
        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .nameUser(profil.getFirstName() + " " + profil.getLastName())
                .email(user.getUsername())
                .userProfile(userProfileMap(profil))
                .build();
    }

    private UsersProfil primaryProfile(Users user) {
        return user.getProfils().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Profil utilisateur introuvable"));
    }

    private boolean isSelfRegistrableRole(RoleType roleType) {
        return roleType == RoleType.PARENT || roleType == RoleType.ELEVE;
    }

    private Map<String, Object> userProfileMap(UsersProfil profil) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", profil.getId());
        profile.put("firstName", profil.getFirstName());
        profile.put("lastName", profil.getLastName());
        profile.put("roleType", profil.getRoleType() == null ? null : profil.getRoleType().name());
        profile.put("roleCode", profil.getEffectiveRoleCode());
        profile.put("roleLabel", profil.getEffectiveRoleLabel());
        if (profil.getRole() != null) {
            profile.put("roleId", profil.getRole().getId());
        }
        return profile;
    }

    private String createPersistedRefreshToken(Users user) {
        String refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.save(buildRefreshToken(user, hashToken(refreshToken)));
        return refreshToken;
    }

    private RefreshToken buildRefreshToken(Users user, String tokenHash) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpiration() / 1000));
        return token;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
