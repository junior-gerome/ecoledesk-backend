package com.school.platform.identityaccess.application;

import com.school.platform.identityaccess.domain.model.AuthenticationAuditEvent;
import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.AuthenticationAuditEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationAuditService {

    private final AuthenticationAuditEventRepository auditEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginSuccess(UserAccount user, String clientIp) {
        record(user == null ? null : user.getId(), user == null ? null : user.getUsername(), "LOGIN_SUCCESS", true, null, clientIp);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginFailure(String username, String clientIp, String reason) {
        record(null, username, "LOGIN_FAILURE", false, reason, clientIp);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRefreshSuccess(UserAccount user, String clientIp) {
        record(user == null ? null : user.getId(), user == null ? null : user.getUsername(), "REFRESH_SUCCESS", true, null, clientIp);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRefreshRejected(Long userId, String username, String clientIp, String reason) {
        record(userId, username, "REFRESH_REJECTED", false, reason, clientIp);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordRefreshReuse(UserAccount user, String clientIp) {
        record(user == null ? null : user.getId(), user == null ? null : user.getUsername(), "REFRESH_REUSE", false, "Refresh token reuse detected", clientIp);
    }

    private void record(Long userId, String username, String eventType, boolean successful, String reason, String clientIp) {
        try {
            AuthenticationAuditEvent event = new AuthenticationAuditEvent();
            event.setUserId(userId);
            event.setUsername(truncate(username, 100));
            event.setEventType(eventType);
            event.setSuccessful(successful);
            event.setReason(truncate(reason, 255));
            event.setClientIp(truncate(clientIp, 45));
            auditEventRepository.save(event);
        } catch (RuntimeException ex) {
            log.warn("Authentication audit event could not be persisted: {}", ex.getMessage());
        }
    }

    private String truncate(String value, int maxLength) {
        return value == null || value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
