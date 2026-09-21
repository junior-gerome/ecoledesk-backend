package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.useraccount.UserAccountBasicDTO;
import com.school.platform.identityaccess.application.dto.useraccount.UserAccountFullDTO;
import com.school.platform.identityaccess.application.dto.useraccount.UserAccountMediumDTO;
import com.school.platform.identityaccess.domain.model.Role;
import com.school.platform.identityaccess.domain.model.UserAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {PersonMapper.class, RoleMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserAccountMapper {

    @Mapping(target = "person", source = "person")
    UserAccountBasicDTO toBasicDTO(UserAccount account);

    @Mapping(target = "firstName", expression = "java(account.getPerson() != null ? account.getPerson().getFirstName() : null)")
    @Mapping(target = "lastName", expression = "java(account.getPerson() != null ? account.getPerson().getLastName() : null)")
    @Mapping(target = "email", expression = "java(account.getPerson() != null ? account.getPerson().getEmail() : null)")
    @Mapping(target = "roleCode", expression = "java(primaryRole(account) != null ? primaryRole(account).getCode() : null)")
    @Mapping(target = "roleLabel", expression = "java(primaryRole(account) != null ? primaryRole(account).getLabel() : null)")
    @Mapping(target = "status", expression = "java(account.getStatus() != null ? account.getStatus().name() : (Boolean.TRUE.equals(account.getEnabled()) ? \"ACTIVE\" : \"SUSPENDED\"))")
    UserAccountMediumDTO toMediumDTO(UserAccount account);

    @Mapping(target = "person", source = "person")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "permissions", ignore = true)
    UserAccountFullDTO toFullDTO(UserAccount account);

    List<UserAccountBasicDTO> toBasicDTOList(List<UserAccount> accounts);

    List<UserAccountMediumDTO> toMediumDTOList(List<UserAccount> accounts);

    default Role primaryRole(UserAccount account) {
        if (account == null || account.getRoles() == null) return null;
        return account.getRoles().stream()
                .filter(role -> role != null)
                .min(Comparator.comparing(role -> role.getCode() == null ? "" : role.getCode()))
                .orElse(null);
    }
}
