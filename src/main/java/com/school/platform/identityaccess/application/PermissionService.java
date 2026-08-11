package com.school.platform.identityaccess.application;

import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.identityaccess.application.dto.permission.PermissionFullDTO;
import com.school.platform.identityaccess.application.mapper.PermissionMapper;
import com.school.platform.identityaccess.domain.model.Permission;
import com.school.platform.identityaccess.infrastructure.persistence.PermissionRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;

import lombok.RequiredArgsConstructor;

// Legacy class kept for reference - superseded by application.impl.PermissionServiceImpl
// @Service
@RequiredArgsConstructor
@Transactional
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionFullDTO create(PermissionFullDTO dto) {
        String code = normalizePermissionCode(dto.getCode());
        if (permissionRepository.existsByCodeIgnoreCase(code)) {
            throw new ValidationException("Code permission deja utilise");
        }

        Permission permission = permissionMapper.toEntity(dto);
        permission.setCode(code);
        if (permission.getActive() == null) {
            permission.setActive(true);
        }

        return permissionMapper.toFullDTO(permissionRepository.save(permission));
    }

    public PermissionFullDTO update(Long id, PermissionFullDTO dto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee"));

        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String nextCode = normalizePermissionCode(dto.getCode());
            if (!permission.getCode().equalsIgnoreCase(nextCode)
                    && permissionRepository.existsByCodeIgnoreCase(nextCode)) {
                throw new ValidationException("Code permission deja utilise");
            }
            permission.setCode(nextCode);
        }

        permission.setDescription(dto.getDescription());
        permission.setResource(dto.getResource());
        permission.setAction(dto.getAction());
        if (dto.getActive() != null) {
            permission.setActive(dto.getActive());
        }

        return permissionMapper.toFullDTO(permissionRepository.save(permission));
    }

    @Transactional(readOnly = true)
    public PermissionFullDTO findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee"));
        return permissionMapper.toFullDTO(permission);
    }

    @Transactional(readOnly = true)
    public Page<PermissionFullDTO> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toFullDTO);
    }

    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee"));
        permission.setActive(false);
        permissionRepository.save(permission);
    }

    private String normalizePermissionCode(String code) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("Le code de la permission est obligatoire");
        }
        String normalized = code.trim().replace(' ', '_');
        if (normalized.regionMatches(true, 0, "ROLE_", 0, "ROLE_".length())) {
            return normalized.toUpperCase(Locale.ROOT);
        }
        if (normalized.length() > 50) {
            throw new ValidationException("Le code de la permission ne doit pas depasser 50 caracteres");
        }
        return normalized;
    }
}