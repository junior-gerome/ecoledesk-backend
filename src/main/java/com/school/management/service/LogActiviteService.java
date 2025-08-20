package com.school.management.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.management.model.LogActivite;
import com.school.management.model.Users;
import com.school.management.repository.LogActiviteRepository;
import com.school.management.repository.UsersRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogActiviteService {
    private final LogActiviteRepository logActiviteRepository;
    private final UsersRepository userRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Transactional
    public void logAction(Long utilisateurId, String action, String tableCible, Long referenceId) {
        Users utilisateur = userRepository.findById(utilisateurId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        LogActivite log = new LogActivite();
        log.setUsers(utilisateur);
        log.setAction(action);
        log.setIpAdresse(getClientIp());
        log.setTableCible(tableCible);
        log.setReferenceId(referenceId);

        logActiviteRepository.save(log);
    }

    public List<LogActivite> getLogsByActivites(Long utilisateurId) {
        List<LogActivite> logs = logActiviteRepository.findByUsersId(utilisateurId);
        if(logs.isEmpty()) {
            throw new RuntimeException("Aucun log trouvé pour l'utilisateur avec ID: " + utilisateurId);
        }
        return logs;
    }

    @Transactional(readOnly = true)
    public List<LogActivite> getLogsByDateRange(LocalDateTime debut, LocalDateTime fin) {
        return logActiviteRepository.findByDateActionBetween(debut, fin);
    }

    @Transactional(readOnly = true)
    public List<LogActivite> getLogsByTable(String tableCible) {
        return logActiviteRepository.findByTableCible(tableCible);
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }
}
