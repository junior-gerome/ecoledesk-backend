package com.school.platform.identityaccess.application.impl;

import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventFullDTO;
import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventMediumDTO;
import com.school.platform.identityaccess.application.interfaces.IAuthenticationAuditQueryService;
import com.school.platform.identityaccess.application.mapper.AuthenticationAuditEventMapper;
import com.school.platform.identityaccess.domain.model.AuthenticationAuditEvent;
import com.school.platform.identityaccess.infrastructure.persistence.AuthenticationAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthenticationAuditQueryServiceImpl implements IAuthenticationAuditQueryService {

    private final AuthenticationAuditEventRepository auditEventRepository;
    private final AuthenticationAuditEventMapper auditEventMapper;

    @Override
    public AuthenticationAuditEventFullDTO findById(Long id) {
        AuthenticationAuditEvent event = auditEventRepository.findById(id)
                .orElseThrow(() -> new com.school.platform.shared.domain.exception.compat.NotFoundException("Authentication audit event non trouve"));
        return auditEventMapper.toFullDTO(event);
    }

    @Override
    public Page<AuthenticationAuditEventMediumDTO> findAll(Pageable pageable) {
        return auditEventRepository.findAll(pageable).map(auditEventMapper::toMediumDTO);
    }
}
