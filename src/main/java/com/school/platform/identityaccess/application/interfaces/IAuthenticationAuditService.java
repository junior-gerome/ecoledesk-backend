package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.domain.model.UserAccount;

public interface IAuthenticationAuditService {
    void recordLoginSuccess(UserAccount user, String clientIp);
    void recordLoginFailure(String username, String clientIp, String reason);
    void recordRefreshSuccess(UserAccount user, String clientIp);
    void recordRefreshRejected(Long userId, String username, String clientIp, String reason);
    void recordRefreshReuse(UserAccount user, String clientIp);
}
