package com.school.platform.support.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.school.platform.support.application.SupportService;
import static org.mockito.ArgumentMatchers.any;

import com.school.platform.support.application.SupportService;
import static org.mockito.Mockito.when;


import com.school.platform.support.application.SupportService;
import com.school.platform.support.application.dto.SupportTicketPayloadDTO;
import com.school.platform.support.application.dto.SupportTicketPriority;
import com.school.platform.support.application.dto.SupportTicketStatus;
import com.school.platform.support.domain.model.SupportTicket;
import com.school.platform.support.infrastructure.persistence.SupportTicketRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {
    @Mock
    private SupportTicketRepository repository;

    @InjectMocks
    private SupportService service;

    @Test
    void helpCenterReturnsCategoriesAndArticles() {
        var helpCenter = service.getHelpCenter();

        assertThat(helpCenter.categories()).isNotEmpty();
        assertThat(helpCenter.articles()).isNotEmpty();
    }

    @Test
    void createTicketOpensNewTicket() {
        SupportTicket saved = new SupportTicket();
        saved.setId(42L);
        saved.setStatus(SupportTicketStatus.OPEN);
        saved.setCreatedAt(LocalDateTime.now());
        when(repository.save(any(SupportTicket.class))).thenReturn(saved);

        var ticket = service.createTicket(new SupportTicketPayloadDTO(
                "Ada Lovelace",
                "ada@example.com",
                "Connexion",
                "Impossible de se connecter",
                SupportTicketPriority.HIGH
        ));

        assertThat(ticket.id()).isPositive();
        assertThat(ticket.status()).isEqualTo(SupportTicketStatus.OPEN);
        assertThat(ticket.createdAt()).isNotNull();
    }
}
