package com.school.platform.identityaccess.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.platform.identityaccess.domain.model.AuthenticationAuditEvent;

@Repository
public interface AuthenticationAuditEventRepository extends JpaRepository<AuthenticationAuditEvent, Long> {
}
