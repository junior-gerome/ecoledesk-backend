package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.role.RoleBasicDTO;
import com.school.platform.identityaccess.application.dto.role.RoleFullDTO;
import com.school.platform.identityaccess.application.dto.role.RoleMediumDTO;
import com.school.platform.identityaccess.domain.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", uses = {PermissionMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    RoleBasicDTO toBasicDTO(Role role);

    RoleMediumDTO toMediumDTO(Role role);

    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "usersCount", ignore = true)
    RoleFullDTO toFullDTO(Role role);

    @Mapping(target = "permissions", ignore = true)
    Role toEntity(RoleFullDTO dto);

    List<RoleBasicDTO> toBasicDTOList(List<Role> roles);

    Set<RoleBasicDTO> toBasicDTOSet(Set<Role> roles);
}
