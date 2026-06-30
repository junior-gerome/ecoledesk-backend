package com.school.platform.identityaccess.application.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.school.platform.identityaccess.application.dto.role.RoleBasicDTO;
import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import com.school.platform.identityaccess.domain.model.Role;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleBasicDTO toBasicDTO(Role role) {
        if (role == null) return null;

        RoleBasicDTO dto = new RoleBasicDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setLabel(role.getLabel());
        return dto;
    }

    public RoleFullDTO toFullDTO(Role role) {
        if (role == null) return null;

        RoleFullDTO dto = new RoleFullDTO();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setLabel(role.getLabel());
        dto.setDescription(role.getDescription());
        dto.setPermissions(role.getPermissions() == null
                ? Set.of()
                : role.getPermissions().stream()
                        .map(permissionMapper::toBasicDTO)
                        .collect(Collectors.toSet()));
        dto.setActive(role.getActive());
        dto.setCreationDate(role.getCreationDate());
        dto.setUpdateDate(role.getUpdateDate());
        return dto;
    }

    public Role toEntity(RoleFullDTO dto) {
        if (dto == null) return null;

        Role role = new Role();
        role.setId(dto.getId());
        role.setCode(dto.getCode());
        role.setLabel(dto.getLabel());
        role.setDescription(dto.getDescription());
        role.setActive(dto.getActive());
        return role;
    }
}