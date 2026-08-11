package com.school.platform.identityaccess.application;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.identityaccess.application.dto.permission.PermissionBasicDTO;
import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import com.school.platform.identityaccess.application.mapper.RoleMapper;
import com.school.platform.identityaccess.domain.model.Permission;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.infrastructure.persistence.PermissionRepository;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;

import lombok.RequiredArgsConstructor;

// Legacy class kept for reference - superseded by application.impl.RoleServiceImpl
// @Service
@RequiredArgsConstructor
@Transactional
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    public RoleFullDTO create(RoleFullDTO dto) {
        String code = normalizeRoleCode(dto.getCode());
        if (roleRepository.existsByCodeIgnoreCase(code)) {
            throw new ValidationException("Code role deja utilise");
        }

        Role role = roleMapper.toEntity(dto);
        role.setCode(code);
        if (role.getActive() == null) {
            role.setActive(true);
        }
        role.setPermissions(resolvePermissions(dto.getPermissions()));

        return roleMapper.toFullDTO(roleRepository.save(role));
    }

    public RoleFullDTO update(Long id, RoleFullDTO dto) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String nextCode = normalizeRoleCode(dto.getCode());
            if (!role.getCode().equalsIgnoreCase(nextCode) && roleRepository.existsByCodeIgnoreCase(nextCode)) {
                throw new ValidationException("Code role deja utilise");
            }
            role.setCode(nextCode);
        }

        role.setLabel(dto.getLabel());
        role.setDescription(dto.getDescription());
        if (dto.getActive() != null) {
            role.setActive(dto.getActive());
        }
        if (dto.getPermissions() != null) {
            role.setPermissions(resolvePermissions(dto.getPermissions()));
        }

        return roleMapper.toFullDTO(roleRepository.save(role));
    }

    public void assignPermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));

        Set<Permission> permissions = permissionIds == null
                ? Set.of()
                : permissionIds.stream()
                        .map(id -> permissionRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + id)))
                        .collect(Collectors.toSet());

        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public RoleFullDTO findById(Long id) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));
        return roleMapper.toFullDTO(role);
    }

    @Transactional(readOnly = true)
    public Page<RoleFullDTO> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(roleMapper::toFullDTO);
    }

    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));
        role.setActive(false);
        roleRepository.save(role);
    }

    private Set<Permission> resolvePermissions(Set<PermissionBasicDTO> permissionDtos) {
        if (permissionDtos == null || permissionDtos.isEmpty()) {
            return Set.of();
        }

        return permissionDtos.stream()
                .map(this::resolvePermission)
                .collect(Collectors.toSet());
    }

    private Permission resolvePermission(PermissionBasicDTO dto) {
        if (dto.getId() != null) {
            return permissionRepository.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + dto.getId()));
        }
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String code = normalizePermissionCode(dto.getCode());
            return permissionRepository.findByCodeIgnoreCase(code)
                    .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + code));
        }
        throw new ValidationException("Permission invalide");
    }

    private String normalizeRoleCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Le code du role est obligatoire");
        }
        String normalized = code.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring("ROLE_".length());
        }
        if (!normalized.matches("[A-Z0-9_]{2,50}")) {
            throw new ValidationException("Le code du role doit contenir uniquement lettres, chiffres et underscores");
        }
        return normalized;
    }

    private String normalizePermissionCode(String code) {
        String normalized = code.trim().replace(' ', '_');
        if (normalized.regionMatches(true, 0, "ROLE_", 0, "ROLE_".length())) {
            return normalized.toUpperCase(Locale.ROOT);
        }
        return normalized;
    }
}