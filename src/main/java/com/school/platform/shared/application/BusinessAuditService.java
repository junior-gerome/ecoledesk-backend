package com.school.platform.shared.application;

import com.school.platform.identityaccess.domain.model.Users;
import com.school.platform.identityaccess.infrastructure.persistence.UsersRepository;
import com.school.platform.reporting.domain.model.LogActivite;
import com.school.platform.reporting.infrastructure.persistence.LogActiviteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessAuditService {

    private final LogActiviteRepository logActiviteRepository;
    private final UsersRepository usersRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String tableCible, Long referenceId) {
        currentUserId().ifPresent(userId -> {
            try {
                Users user = usersRepository.findById(userId).orElse(null);
                if (user == null) {
                    return;
                }

                LogActivite log = new LogActivite();
                log.setUsers(user);
                log.setAction(action);
                log.setTableCible(tableCible);
                log.setReferenceId(referenceId);
                logActiviteRepository.save(log);
            } catch (RuntimeException ex) {
                log.warn("Business audit failed for action {} on {}#{}: {}",
                        action, tableCible, referenceId, ex.getMessage());
            }
        });
    }

    public java.util.Optional<Long> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return java.util.Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.school.platform.identityaccess.infrastructure.security.AuthenticatedUserPrincipal user) {
            return java.util.Optional.ofNullable(user.id());
        }
        return java.util.Optional.empty();
    }
}
