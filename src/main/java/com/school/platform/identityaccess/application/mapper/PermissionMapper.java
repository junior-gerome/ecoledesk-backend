package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.permission.PermissionBasicDTO;
import com.school.platform.identityaccess.application.dto.permission.PermissionFullDTO;
import com.school.platform.identityaccess.application.dto.permission.PermissionMediumDTO;
import com.school.platform.identityaccess.domain.model.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PermissionMapper {

    PermissionBasicDTO toBasicDTO(Permission permission);

    PermissionMediumDTO toMediumDTO(Permission permission);

    PermissionFullDTO toFullDTO(Permission permission);

    @Mapping(target = "scope", ignore = true)
    Permission toEntity(PermissionFullDTO dto);

    List<PermissionBasicDTO> toBasicDTOList(List<Permission> permissions);

    Set<PermissionBasicDTO> toBasicDTOSet(Set<Permission> permissions);
}
