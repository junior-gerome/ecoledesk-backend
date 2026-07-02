package com.school.platform.identityaccess.application;

import com.school.platform.identityaccess.application.dto.useraccount.*;
import com.school.platform.identityaccess.application.mapper.RoleMapper;
import com.school.platform.identityaccess.domain.model.Person;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.PersonRepository;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Deprecated(since = "2026-07", forRemoval = false)
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PersonRepository personRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;

    public UserAccountFullDTO create(Long personId, String username, String password) {
        if (userAccountRepository.existsByUsername(username)) {
            throw new ValidationException("Nom d'utilisateur déjà utilisé");
        }
        
        Person person = personRepository.findById(personId)
            .orElseThrow(() -> new NotFoundException("Personne non trouvée"));
        
        UserAccount account = new UserAccount();
        account.setPerson(person);
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEnabled(true);
        
        account = userAccountRepository.save(account);
        return toFullDTO(account);
    }

    public void assignRoles(Long userId, Set<Long> roleIds) {
        UserAccount account = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        
        Set<Role> roles = roleIds.stream()
            .map(id -> roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rôle non trouvé: " + id)))
            .collect(Collectors.toSet());
        
        account.setRoles(roles);
        userAccountRepository.save(account);
    }

    public void changePassword(Long userId, String newPassword) {
        UserAccount account = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        
        account.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
    }

    public void toggleEnabled(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        
        account.setEnabled(!account.getEnabled());
        userAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public UserAccountFullDTO findById(Long id) {
        UserAccount account = userAccountRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
        return toFullDTO(account);
    }

    @Transactional(readOnly = true)
    public Page<UserAccountMediumDTO> findAll(Pageable pageable) {
        return userAccountRepository.findAll(pageable)
            .map(this::toMediumDTO);
    }

    private UserAccountFullDTO toFullDTO(UserAccount account) {
        UserAccountFullDTO dto = new UserAccountFullDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setEnabled(account.getEnabled());
        dto.setEmailVerified(account.getEmailVerified());
        dto.setLastLogin(account.getLastLogin());
        dto.setRoles(account.getRoles().stream()
            .map(roleMapper::toBasicDTO)
            .collect(Collectors.toSet()));
        dto.setCreationDate(account.getCreationDate());
        dto.setUpdateDate(account.getUpdateDate());
        return dto;
    }

    private UserAccountMediumDTO toMediumDTO(UserAccount account) {
        UserAccountMediumDTO dto = new UserAccountMediumDTO();
        dto.setId(account.getId());
        dto.setUsername(account.getUsername());
        dto.setEnabled(account.getEnabled());
        dto.setEmailVerified(account.getEmailVerified());
        dto.setLastLogin(account.getLastLogin());
        dto.setRolesCount(account.getRoles().size());
        return dto;
    }
}
