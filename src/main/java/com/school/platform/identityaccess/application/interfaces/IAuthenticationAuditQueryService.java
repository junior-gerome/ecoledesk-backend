package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventFullDTO;
import com.school.platform.identityaccess.application.dto.audit.AuthenticationAuditEventMediumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IAuthenticationAuditQueryService {
    AuthenticationAuditEventFullDTO findById(Long id);
    Page<AuthenticationAuditEventMediumDTO> findAll(Pageable pageable);
}
