package com.school.platform.reporting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.reporting.application.dto.audit.AuditLogDTO;
import com.school.platform.reporting.domain.model.LogActivite;
import com.school.platform.reporting.infrastructure.persistence.LogActiviteRepository;
import com.school.platform.shared.web.PageResponse;

class AuditControllerPaginationTest {

    private final LogActiviteRepository logActiviteRepository = mock(LogActiviteRepository.class);
    private final AuditController controller = new AuditController(logActiviteRepository);

    private LogActivite log() {
        UserAccount user = new UserAccount();
        user.setUsername("admin");
        LogActivite log = new LogActivite();
        log.setId(1L);
        log.setUserAccount(user);
        log.setAction("CREATE");
        log.setDateAction(LocalDateTime.of(2026, 1, 2, 10, 0));
        log.setIpAdresse("::1");
        log.setTableCible("students");
        log.setReferenceId(7L);
        return log;
    }

    @Test
    void getLogsClampsNegativePageAndExcessiveSize() {
        LogActivite entry = log();
        when(logActiviteRepository.findAllByOrderByDateActionDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(0, 200), 1));

        ResponseEntity<PageResponse<AuditLogDTO>> response = controller.getLogs(-5, 500, null);

        PageRequest request = capturedRequest();
        assertThat(request.getPageNumber()).isEqualTo(0);
        assertThat(request.getPageSize()).isEqualTo(200);

        PageResponse<AuditLogDTO> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content().getFirst().getUsername()).isEqualTo("admin");
    }

    @Test
    void getLogsAcceptsLimitAsAliasOfSize() {
        LogActivite entry = log();
        when(logActiviteRepository.findAllByOrderByDateActionDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(1, 25), 1));

        ResponseEntity<PageResponse<AuditLogDTO>> response = controller.getLogs(1, null, 25);

        PageRequest request = capturedRequest();
        assertThat(request.getPageNumber()).isEqualTo(1);
        assertThat(request.getPageSize()).isEqualTo(25);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getLogsDefaultsSizeToFifty() {
        when(logActiviteRepository.findAllByOrderByDateActionDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

        controller.getLogs(0, null, null);

        PageRequest request = capturedRequest();
        assertThat(request.getPageSize()).isEqualTo(50);
    }

    private PageRequest capturedRequest() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(logActiviteRepository).findAllByOrderByDateActionDesc(captor.capture());
        return (PageRequest) captor.getValue();
    }
}