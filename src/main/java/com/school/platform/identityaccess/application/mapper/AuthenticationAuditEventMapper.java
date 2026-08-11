package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventBasicDTO;
import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventFullDTO;
import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventMediumDTO;
import com.school.platform.identityaccess.domain.model.AuthenticationAuditEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthenticationAuditEventMapper {

    AuthenticationAuditEventBasicDTO toBasicDTO(AuthenticationAuditEvent event);

    AuthenticationAuditEventMediumDTO toMediumDTO(AuthenticationAuditEvent event);

    AuthenticationAuditEventFullDTO toFullDTO(AuthenticationAuditEvent event);

    AuthenticationAuditEvent toEntity(AuthenticationAuditEventFullDTO dto);

    List<AuthenticationAuditEventBasicDTO> toBasicDTOList(List<AuthenticationAuditEvent> events);

    List<AuthenticationAuditEventMediumDTO> toMediumDTOList(List<AuthenticationAuditEvent> events);

    List<AuthenticationAuditEventFullDTO> toFullDTOList(List<AuthenticationAuditEvent> events);
}
