package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.dto.permission.PermissionBasicDTO;
import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import com.school.platform.identityaccess.application.interfaces.IRoleService;
import com.school.platform.identityaccess.application.mapper.RoleMapper;
import com.school.platform.identityaccess.domain.model.Permission;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.infrastructure.persistence.PermissionRepository;
import com.school.platform.identityaccess.infrastructure.persistence.RoleRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleServiceImpl implements IRoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleFullDTO create(RoleFullDTO dto) {
        String code = normalizeRoleCode(dto.getCode());
        if (roleRepository.existsByCodeIgnoreCase(code)) throw new ValidationException("Code role deja utilise");
        Role role = roleMapper.toEntity(dto);
        role.setCode(code);
        if (role.getActive() == null) role.setActive(true);
        role.setPermissions(resolvePermissions(dto.getPermissions()));
        return roleMapper.toFullDTO(roleRepository.save(role));
    }

    @Override
    public RoleFullDTO update(Long id, RoleFullDTO dto) {
        Role role = roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String next = normalizeRoleCode(dto.getCode());
            if (!role.getCode().equalsIgnoreCase(next) && roleRepository.existsByCodeIgnoreCase(next)) {
                throw new ValidationException("Code role deja utilise");
            }
            role.setCode(next);
        }
        role.setLabel(dto.getLabel());
        role.setDescription(dto.getDescription());
        if (dto.getActive() != null) role.setActive(dto.getActive());
        if (dto.getPermissions() != null) role.setPermissions(resolvePermissions(dto.getPermissions()));
        return roleMapper.toFullDTO(roleRepository.save(role));
    }

    @Override
    public void assignPermissions(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findByIdWithPermissions(roleId)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));
        role.setPermissions(permissionIds == null ? Set.of() : permissionIds.stream()
                .map(id -> permissionRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + id)))
                .collect(Collectors.toSet()));
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleFullDTO findById(Long id) {
        return roleMapper.toFullDTO(roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve")));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleFullDTO> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::toFullDTO);
    }

    @Override
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Role non trouve"));
        role.setActive(false);
        roleRepository.save(role);
    }

    private Set<Permission> resolvePermissions(Set<PermissionBasicDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) return Set.of();
        return dtos.stream().map(dto -> {
            if (dto.getId() != null) return permissionRepository.findById(dto.getId())
                    .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + dto.getId()));
            if (dto.getCode() != null && !dto.getCode().isBlank())
                return permissionRepository.findByCodeIgnoreCase(dto.getCode())
                        .orElseThrow(() -> new NotFoundException("Permission non trouvee: " + dto.getCode()));
            throw new ValidationException("Permission invalide");
        }).collect(Collectors.toSet());
    }

    private String normalizeRoleCode(String code) {
        if (code == null || code.isBlank()) throw new ValidationException("Le code du role est obligatoire");
        String normalized = code.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        if (!normalized.matches("[A-Z0-9_]{2,50}")) throw new ValidationException("Code role invalide");
        return normalized;
    }
}
