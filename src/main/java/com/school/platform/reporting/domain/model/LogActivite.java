package com.school.platform.reporting.domain.model;

import java.time.LocalDateTime;

import com.school.platform.identityaccess.domain.model.UserAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "log_activite")
public class LogActivite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount userAccount;

    @Column(nullable = false, length = 255) private String action;
    @Column(name = "date_action", insertable = false, updatable = false) private LocalDateTime dateAction;
    @Column(name = "ip_adresse", length = 45) private String ipAdresse;
    @Column(name = "table_cible", length = 100) private String tableCible;
    @Column(name = "reference_id") private Long referenceId;
}
