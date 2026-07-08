package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.permission.*;
import com.school.platform.identityaccess.domain.model.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionBasicDTO toBasicDTO(Permission permission) {
        if (permission == null) return null;
        
        PermissionBasicDTO dto = new PermissionBasicDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setDescription(permission.getDescription());
        return dto;
    }

    public PermissionFullDTO toFullDTO(Permission permission) {
        if (permission == null) return null;
        
        PermissionFullDTO dto = new PermissionFullDTO();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setDescription(permission.getDescription());
        dto.setResource(permission.getResource());
        dto.setAction(permission.getAction());
        dto.setActive(permission.getActive());
        dto.setCreationDate(permission.getCreationDate());
        dto.setUpdateDate(permission.getUpdateDate());
        return dto;
    }

    public Permission toEntity(PermissionFullDTO dto) {
        if (dto == null) return null;
        
        Permission permission = new Permission();
        permission.setId(dto.getId());
        permission.setCode(dto.getCode());
        permission.setDescription(dto.getDescription());
        permission.setResource(dto.getResource());
        permission.setAction(dto.getAction());
        permission.setActive(dto.getActive());
        return permission;
    }
}
