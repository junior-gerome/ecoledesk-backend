package com.school.management.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "log_activite")
public class LogActivite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(name = "date_action", insertable = false, updatable = false)
    private LocalDateTime dateAction;

    @Column(name = "ip_adresse", length = 45)
    private String ipAdresse;

    @Column(name = "table_cible", length = 100)
    private String tableCible;

    @Column(name = "reference_id")
    private Long referenceId;
}
