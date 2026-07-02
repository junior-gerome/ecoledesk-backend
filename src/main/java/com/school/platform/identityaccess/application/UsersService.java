package com.school.platform.identityaccess.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.identityaccess.application.dto.UsersDTO;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.domain.model.UsersProfil;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersProfilRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository userRepository;
    private final UsersProfilRepository profilRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetDeliveryService passwordResetDeliveryService;
    private final PasswordResetRateLimitService passwordResetRateLimitService;

    @Transactional(readOnly = true)
    public List<UsersDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public UsersDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
    }

    @Transactional
    public UsersDTO registerUser(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getEmail())) {
            throw new BusinessException("Email deja utilise");
        }

        Users user = new Users();
        user.setUsername(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setActif(true);
        user = userRepository.save(user);

        UsersProfil profil = new UsersProfil();
        profil.setEmailUser(req.getEmail());
        profil.setFirstName(req.getFirstName());
        profil.setLastName(req.getLastName());
        profil.setRoleType(req.getRoleType() == null ? RoleType.AGENT : req.getRoleType());
        profil.setReferenceId(req.getReferenceId());
        profil.setUser(user);
        if (req.getRoleId() != null) {
            profil.setRole(findActiveRole(req.getRoleId()));
        }
        profilRepository.save(profil);
        user.setProfils(List.of(profil));

        return convertToDTO(user);
    }

    @Transactional
    public UsersDTO updateUser(Long id, UsersDTO dto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));

        String nextUsername = firstNonBlank(dto.getEmail(), dto.getUsername(), user.getUsername());
        if (!user.getUsername().equals(nextUsername) && userRepository.existsByUsername(nextUsername)) {
            throw new BusinessException("Username deja utilise");
        }
        user.setUsername(nextUsername);
        user.setActif(!"SUSPENDED".equalsIgnoreCase(String.valueOf(dto.getStatus())));

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        UsersProfil profil = resolvePrimaryProfile(user);
        profil.setEmailUser(nextUsername);
        profil.setFirstName(firstNonBlank(dto.getFirstName(), profil.getFirstName()));
        profil.setLastName(firstNonBlank(dto.getLastName(), profil.getLastName()));

        if (dto.getRoleId() != null) {
            profil.setRole(findActiveRole(dto.getRoleId()));
        } else if (dto.getRoleCode() != null && !dto.getRoleCode().isBlank()) {
            profil.setRole(findActiveRole(dto.getRoleCode()));
        } else if (dto.getRoleType() != null) {
            profil.setRoleType(dto.getRoleType());
            profil.setRole(null);
        }

        profilRepository.save(profil);

        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public UsersDTO assignRole(Long userId, Long roleId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        UsersProfil profil = resolvePrimaryProfile(user);
        profil.setRole(findActiveRole(roleId));
        profilRepository.save(profil);
        return convertToDTO(user);
    }

    @Transactional
    public UsersDTO clearDynamicRole(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        UsersProfil profil = resolvePrimaryProfile(user);
        profil.setRole(null);
        profilRepository.save(profil);
        return convertToDTO(user);
    }

    @Transactional
    public UsersDTO updateStatus(Long id, String status) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        user.setActif(!"SUSPENDED".equalsIgnoreCase(String.valueOf(status)));
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        user.setActif(false);
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(String email, String clientIp) {
        passwordResetRateLimitService.assertAllowed(email, clientIp);
        userRepository.findByUsername(email).ifPresent(user -> {
            String resetToken = java.util.UUID.randomUUID().toString();
            user.setResetToken(hashToken(resetToken));
            user.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            sendPasswordResetTokenAfterCommit(user.getUsername(), resetToken);
        });
    }

    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        if (user.getResetToken() == null
                || user.getResetTokenExpiresAt() == null
                || user.getResetTokenExpiresAt().isBefore(LocalDateTime.now())
                || !hashToken(resetToken).equals(user.getResetToken())) {
            throw new BusinessException("Token de reinitialisation invalide");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiresAt(null);
        userRepository.save(user);
    }


    private void sendPasswordResetTokenAfterCommit(String username, String resetToken) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                passwordResetDeliveryService.sendResetToken(username, resetToken);
            }
        });
    }
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }

    private UsersDTO convertToDTO(Users user) {
        UsersDTO dto = new UsersDTO();
        UsersProfil profil = user.getProfils() == null || user.getProfils().isEmpty()
                ? null
                : user.getProfils().get(0);
        Role dynamicRole = profil == null ? null : profil.getRole();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(profil != null ? profil.getEmailUser() : user.getUsername());
        dto.setFirstName(profil != null ? profil.getFirstName() : "");
        dto.setLastName(profil != null ? profil.getLastName() : "");
        dto.setRoleType(profil != null ? profil.getRoleType() : RoleType.AGENT);
        if (dynamicRole != null) {
            dto.setRoleId(dynamicRole.getId());
            dto.setRoleCode(dynamicRole.getCode());
            dto.setRoleLabel(dynamicRole.getLabel());
        } else if (profil != null && profil.getRoleType() != null) {
            dto.setRoleCode(profil.getRoleType().name());
            dto.setRoleLabel(profil.getRoleType().name());
        }
        dto.setStatus(Boolean.TRUE.equals(user.getActif()) ? "ACTIVE" : "SUSPENDED");
        dto.setPermissions(resolvePermissions(user));
        dto.setActif(user.getActif());
        dto.setLastLogin(user.getLastLogin());
        dto.setLastLoginAt(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private UsersProfil resolvePrimaryProfile(Users user) {
        if (user.getProfils() != null && !user.getProfils().isEmpty()) {
            return user.getProfils().get(0);
        }

        UsersProfil profil = new UsersProfil();
        profil.setEmailUser(user.getUsername());
        profil.setFirstName("");
        profil.setLastName("");
        profil.setRoleType(RoleType.AGENT);
        profil.setUser(user);
        user.setProfils(new ArrayList<>(List.of(profil)));
        return profil;
    }

    private Role findActiveRole(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        if (Boolean.FALSE.equals(role.getActive())) {
            throw new BusinessException("Role inactif");
        }
        return role;
    }

    private Role findActiveRole(String roleCode) {
        Role role = roleRepository.findWithPermissionsByCode(normalizeRoleCode(roleCode))
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", roleCode));
        if (Boolean.FALSE.equals(role.getActive())) {
            throw new BusinessException("Role inactif");
        }
        return role;
    }

    private String normalizeRoleCode(String roleCode) {
        String normalized = roleCode.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return normalized.startsWith("ROLE_") ? normalized.substring("ROLE_".length()) : normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private List<String> resolvePermissions(Users user) {
        return user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}