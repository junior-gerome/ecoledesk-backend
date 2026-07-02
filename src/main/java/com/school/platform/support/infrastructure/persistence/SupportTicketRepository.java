package com.school.platform.support.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.platform.support.domain.model.SupportTicket;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
}
