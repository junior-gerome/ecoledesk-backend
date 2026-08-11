package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.dto.permission.PermissionFullDTO;
import com.school.platform.identityaccess.application.interfaces.IPermissionService;
import com.school.platform.identityaccess.application.mapper.PermissionMapper;
import com.school.platform.identityaccess.domain.model.Permission;
import com.school.platform.identityaccess.infrastructure.persistence.PermissionRepository;
import com.school.platform.shared.domain.exception.compat.NotFoundException;
import com.school.platform.shared.domain.exception.compat.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionFullDTO create(PermissionFullDTO dto) {
        String code = normalizeCode(dto.getCode());
        if (permissionRepository.existsByCodeIgnoreCase(code)) {
            throw new ValidationException("Code permission deja utilise");
        }
        Permission permission = permissionMapper.toEntity(dto);
        permission.setCode(code);
        if (permission.getActive() == null) permission.setActive(true);
        return permissionMapper.toFullDTO(permissionRepository.save(permission));
    }

    @Override
    public PermissionFullDTO update(Long id, PermissionFullDTO dto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee"));
        if (dto.getCode() != null && !dto.getCode().isBlank()) {
            String next = normalizeCode(dto.getCode());
            if (!permission.getCode().equalsIgnoreCase(next) && permissionRepository.existsByCodeIgnoreCase(next)) {
                throw new ValidationException("Code permission deja utilise");
            }
            permission.setCode(next);
        }
        permission.setDescription(dto.getDescription());
        permission.setResource(dto.getResource());
        permission.setAction(dto.getAction());
        if (dto.getActive() != null) permission.setActive(dto.getActive());
        return permissionMapper.toFullDTO(permissionRepository.save(permission));
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionFullDTO findById(Long id) {
        return permissionMapper.toFullDTO(permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee")));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionFullDTO> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(permissionMapper::toFullDTO);
    }

    @Override
    public void delete(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Permission non trouvee"));
        permission.setActive(false);
        permissionRepository.save(permission);
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) throw new ValidationException("Le code de la permission est obligatoire");
        String normalized = code.trim().replace(' ', '_');
        if (normalized.length() > 50) throw new ValidationException("Le code ne doit pas depasser 50 caracteres");
        return normalized.regionMatches(true, 0, "ROLE_", 0, 5)
                ? normalized.toUpperCase(Locale.ROOT) : normalized;
    }
}
