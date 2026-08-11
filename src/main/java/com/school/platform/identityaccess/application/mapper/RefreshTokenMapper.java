package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenBasicDTO;
import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenFullDTO;
import com.school.platform.identityaccess.application.dto.refreshtoken.RefreshTokenMediumDTO;
import com.school.platform.identityaccess.domain.model.RefreshToken;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RefreshTokenMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "active", expression = "java(token.isActive(java.time.LocalDateTime.now()))")
    RefreshTokenBasicDTO toBasicDTO(RefreshToken token);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "active", expression = "java(token.isActive(java.time.LocalDateTime.now()))")
    RefreshTokenMediumDTO toMediumDTO(RefreshToken token);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "active", expression = "java(token.isActive(java.time.LocalDateTime.now()))")
    RefreshTokenFullDTO toFullDTO(RefreshToken token);

    @Mapping(target = "user", ignore = true)
    RefreshToken toEntity(RefreshTokenFullDTO dto);

    List<RefreshTokenBasicDTO> toBasicDTOList(List<RefreshToken> tokens);

    List<RefreshTokenMediumDTO> toMediumDTOList(List<RefreshToken> tokens);

    List<RefreshTokenFullDTO> toFullDTOList(List<RefreshToken> tokens);
}
