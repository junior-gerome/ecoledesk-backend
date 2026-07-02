package com.school.platform.identityaccess.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "authentication_audit_events",
        indexes = {
                @Index(name = "idx_auth_audit_username", columnList = "username"),
                @Index(name = "idx_auth_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_auth_audit_event_type", columnList = "event_type"),
                @Index(name = "idx_auth_audit_created_at", columnList = "created_at")
        }
)
public class AuthenticationAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "successful", nullable = false)
    private boolean successful;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
