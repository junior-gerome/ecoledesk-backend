package com.school.platform.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.platform.identityaccess.domain.model.AuthenticationAuditEvent;

public interface AuthenticationAuditEventRepository extends JpaRepository<AuthenticationAuditEvent, Long> {
}
