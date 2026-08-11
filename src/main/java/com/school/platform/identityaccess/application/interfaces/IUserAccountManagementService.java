package com.school.platform.identityaccess.application.interfaces;

import com.school.platform.identityaccess.application.dto.UserAccountSummaryDTO;
import com.school.platform.identityaccess.application.dto.auth.RegisterRequest;

import java.util.List;

public interface IUserAccountManagementService {
    List<UserAccountSummaryDTO> getAllAccounts();
    boolean existsByUsername(String username);
    UserAccountSummaryDTO getAccountById(Long id);
    UserAccountSummaryDTO registerAccount(RegisterRequest request);
    UserAccountSummaryDTO updateAccount(Long id, UserAccountSummaryDTO dto);
    UserAccountSummaryDTO assignRole(Long userId, Long roleId);
    UserAccountSummaryDTO clearDynamicRole(Long userId);
    UserAccountSummaryDTO updateStatus(Long id, String status);
    void deactivateAccount(Long id);
    void updateLastLogin(Long id);
    void requestPasswordReset(String email, String clientIp);
    void resetPassword(String email, String resetToken, String newPassword);

    com.school.platform.identityaccess.domain.model.UserAccount createAccount(RegisterRequest request, boolean selfRegistration);
}
