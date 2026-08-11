package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.LoginAttemptService;
import com.school.platform.identityaccess.application.PasswordResetDeliveryService;
import com.school.platform.identityaccess.application.PasswordResetRateLimitService;
import com.school.platform.identityaccess.application.dto.UserAccountSummaryDTO;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;
import com.school.platform.identityaccess.application.interfaces.IUserAccountManagementService;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class UserAccountManagementServiceImpl implements IUserAccountManagementService {

    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetDeliveryService passwordResetDeliveryService;
    private final PasswordResetRateLimitService passwordResetRateLimitService;

    @Override
    @Transactional(readOnly = true)
    public List<UserAccountSummaryDTO> getAllAccounts() {
        return userAccountRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userAccountRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public UserAccountSummaryDTO getAccountById(Long id) {
        return toDto(findAccount(id));
    }

    @Override
    @Transactional
    public UserAccountSummaryDTO registerAccount(RegisterRequest request) {
        return toDto(createAccount(request, false));
    }

    @Override
    @Transactional
    public UserAccountSummaryDTO updateAccount(Long id, UserAccountSummaryDTO dto) {
        UserAccount account = findAccount(id);
        String username = firstNonBlank(dto.getEmail(), dto.getUsername(), account.getUsername());
        if (!account.getUsername().equals(username) && userAccountRepository.existsByUsername(username)) {
            throw new BusinessException("Username deja utilise");
        }
        account.setUsername(username);
        account.setEnabled(!"SUSPENDED".equalsIgnoreCase(dto.getStatus()));
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            account.setPassword(passwordEncoder.encode(dto.getPassword()));
        Person person = account.getPerson();
        person.setFirstName(firstNonBlank(dto.getFirstName(), person.getFirstName()));
        person.setLastName(firstNonBlank(dto.getLastName(), person.getLastName()));
        person.setEmail(username);
        if (dto.getRoleId() != null) account.setRoles(Set.of(findActiveRole(dto.getRoleId())));
        else if (dto.getRoleCode() != null && !dto.getRoleCode().isBlank())
            account.setRoles(Set.of(findActiveRole(dto.getRoleCode())));
        return toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional
    public UserAccountSummaryDTO assignRole(Long userId, Long roleId) {
        UserAccount account = findAccount(userId);
        account.setRoles(Set.of(findActiveRole(roleId)));
        return toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional
    public UserAccountSummaryDTO clearDynamicRole(Long userId) {
        UserAccount account = findAccount(userId);
        account.getRoles().clear();
        return toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional
    public UserAccountSummaryDTO updateStatus(Long id, String status) {
        UserAccount account = findAccount(id);
        account.setEnabled(!"SUSPENDED".equalsIgnoreCase(status));
        return toDto(userAccountRepository.save(account));
    }

    @Override
    @Transactional
    public void deactivateAccount(Long id) {
        UserAccount account = findAccount(id);
        account.setEnabled(false);
        userAccountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateLastLogin(Long id) {
        UserAccount account = findAccount(id);
        account.setLastLogin(LocalDateTime.now());
        userAccountRepository.save(account);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email, String clientIp) {
        passwordResetRateLimitService.assertAllowed(email, clientIp);
        userAccountRepository.findByUsername(email).ifPresent(account -> {
            String token = java.util.UUID.randomUUID().toString();
            account.setResetToken(hashToken(token));
            account.setResetTokenExpiresAt(LocalDateTime.now().plusMinutes(30));
            userAccountRepository.save(account);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { passwordResetDeliveryService.sendResetToken(account.getUsername(), token); }
            });
        });
    }

    @Override
    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        UserAccount account = userAccountRepository.findByUsername(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
        if (account.getResetToken() == null || account.getResetTokenExpiresAt() == null
                || account.getResetTokenExpiresAt().isBefore(LocalDateTime.now())
                || !hashToken(resetToken).equals(account.getResetToken())) {
            throw new BusinessException("Token de reinitialisation invalide");
        }
        account.setPassword(passwordEncoder.encode(newPassword));
        account.setResetToken(null);
        account.setResetTokenExpiresAt(null);
        userAccountRepository.save(account);
    }

    @Override
    public UserAccount createAccount(RegisterRequest request, boolean selfRegistration) {
        if (userAccountRepository.existsByUsername(request.getEmail()))
            throw new BusinessException("Email deja utilise");
        Role role = findActiveRole(request);
        if (selfRegistration && !Set.of("PARENT", "ELEVE").contains(role.getCode().toUpperCase()))
            throw new BusinessException("Ce type de profil doit etre cree par un administrateur.");
        Person person = new Person();
        person.setFirstName(request.getFirstName());
        person.setLastName(request.getLastName());
        person.setEmail(request.getEmail());
        UserAccount account = new UserAccount();
        account.setPerson(person);
        account.setUsername(request.getEmail());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setEnabled(true);
        account.setRoles(Set.of(role));
        return userAccountRepository.save(account);
    }

    private UserAccount findAccount(Long id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouve"));
    }

    private Role findActiveRole(RegisterRequest request) {
        if (request.getRoleId() != null) return findActiveRole(request.getRoleId());
        return findActiveRole(firstNonBlank(request.getRoleCode(), "AGENT"));
    }

    private Role findActiveRole(Long roleId) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));
        if (Boolean.FALSE.equals(role.getActive())) throw new BusinessException("Role inactif");
        return role;
    }

    private Role findActiveRole(String roleCode) {
        Role role = roleRepository.findWithPermissionsByCode(normalizeRoleCode(roleCode))
                .orElseThrow(() -> new ResourceNotFoundException("Role", "code", roleCode));
        if (Boolean.FALSE.equals(role.getActive())) throw new BusinessException("Role inactif");
        return role;
    }

    private UserAccountSummaryDTO toDto(UserAccount account) {
        UserAccountSummaryDTO dto = new UserAccountSummaryDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setEmail(account.getPerson().getEmail());
        dto.setFirstName(account.getPerson().getFirstName());
        dto.setLastName(account.getPerson().getLastName());
        Role primaryRole = account.getRoles().stream().findFirst().orElse(null);
        if (primaryRole != null) {
            dto.setRoleId(primaryRole.getId());
            dto.setRoleCode(primaryRole.getCode());
            dto.setRoleLabel(primaryRole.getLabel());
        }
        dto.setStatus(account.isEnabled() ? "ACTIVE" : "SUSPENDED");
        dto.setPermissions(account.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        dto.setActif(account.isEnabled());
        dto.setLastLogin(account.getLastLogin());
        dto.setLastLoginAt(account.getLastLogin());
        dto.setCreatedAt(account.getCreationDate());
        return dto;
    }

    private String normalizeRoleCode(String roleCode) {
        String normalized = roleCode.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return normalized.startsWith("ROLE_") ? normalized.substring(5) : normalized;
    }

    private String firstNonBlank(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v.trim();
        return "";
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible", e);
        }
    }
}
