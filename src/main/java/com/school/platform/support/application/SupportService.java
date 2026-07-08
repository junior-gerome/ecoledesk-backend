package com.school.platform.support.application;

import com.school.platform.support.application.dto.HelpArticleDTO;
import com.school.platform.support.application.dto.HelpCategoryDTO;
import com.school.platform.support.application.dto.HelpCenterDataDTO;
import com.school.platform.support.application.dto.SupportTicketDTO;
import com.school.platform.support.application.dto.SupportTicketPayloadDTO;
import com.school.platform.support.application.dto.SupportTicketStatus;
import com.school.platform.support.domain.model.SupportTicket;
import com.school.platform.support.infrastructure.persistence.SupportTicketRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SupportService {
    private final SupportTicketRepository repository;

    @Transactional(readOnly = true)
    public HelpCenterDataDTO getHelpCenter() {
        return new HelpCenterDataDTO(
                List.of(
                        new HelpCategoryDTO(1, "Comptes et securite"),
                        new HelpCategoryDTO(2, "Scolarite"),
                        new HelpCategoryDTO(3, "Paiements")
                ),
                List.of(
                        new HelpArticleDTO(1, "Reinitialiser un mot de passe",
                                "Utilisez le lien mot de passe oublie sur l'ecran de connexion.",
                                "Comptes et securite", List.of("auth", "password")),
                        new HelpArticleDTO(2, "Creer une inscription",
                                "Ouvrez Eleves puis Ajouter un eleve pour creer la fiche et l'inscription.",
                                "Scolarite", List.of("eleves", "inscription")),
                        new HelpArticleDTO(3, "Generer un recu",
                                "Depuis Paiements, ouvrez les recus puis exportez le document.",
                                "Paiements", List.of("paiement", "recu"))
                )
        );
    }

    @Transactional
    public SupportTicketDTO createTicket(SupportTicketPayloadDTO payload) {
        SupportTicket ticket = new SupportTicket();
        ticket.setName(payload.name());
        ticket.setEmail(payload.email());
        ticket.setSubject(payload.subject());
        ticket.setMessage(payload.message());
        ticket.setPriority(payload.priority());
        ticket.setStatus(SupportTicketStatus.OPEN);
        ticket.setCreatedAt(LocalDateTime.now());

        SupportTicket saved = repository.save(ticket);
        return new SupportTicketDTO(saved.getId(), saved.getStatus(), saved.getCreatedAt());
    }
}
