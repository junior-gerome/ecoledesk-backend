package com.school.platform.reporting.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;
import com.school.platform.reporting.domain.model.LogActivite;
import com.school.platform.reporting.infrastructure.persistence.LogActiviteRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogActiviteService {
    private final LogActiviteRepository logActiviteRepository;
    private final UserAccountRepository userAccountRepository;
    @Autowired private HttpServletRequest httpServletRequest;

    @Transactional
    public void logAction(Long userId, String action, String tableCible, Long referenceId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));
        LogActivite log = new LogActivite();
        log.setUserAccount(account);
        log.setAction(action);
        log.setIpAdresse(getClientIp());
        log.setTableCible(tableCible);
        log.setReferenceId(referenceId);
        logActiviteRepository.save(log);
    }

    public List<LogActivite> getLogsByActivites(Long userId) {
        List<LogActivite> logs = logActiviteRepository.findByUserAccountId(userId);
        if (logs.isEmpty()) throw new RuntimeException("Aucun log trouve pour l'utilisateur avec ID: " + userId);
        return logs;
    }

    @Transactional(readOnly = true) public List<LogActivite> getLogsByDateRange(LocalDateTime start, LocalDateTime end) { return logActiviteRepository.findByDateActionBetween(start, end); }
    @Transactional(readOnly = true) public List<LogActivite> getLogsByTable(String tableCible) { return logActiviteRepository.findByTableCible(tableCible); }

    private String getClientIp() {
        String forwarded = httpServletRequest.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isEmpty() ? forwarded.split(",")[0] : httpServletRequest.getRemoteAddr();
    }
}
