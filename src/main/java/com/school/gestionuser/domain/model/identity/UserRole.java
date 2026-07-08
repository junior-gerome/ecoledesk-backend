package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entité de jointure User ↔ Role avec temporalité.
 *
 * Pourquoi une entité et pas @ManyToMany ?
 * - Stocke validFrom / validTo (affectation temporelle)
 * - Stocke assignedBy (traçabilité)
 * - Stocke reason (audit)
 * - Permet la révocation sans suppression physique (validTo = today)
 *
 * Pattern important pour un ERP : toute affectation de rôle est datée et tracée.
 */
@Entity
@Table(name = "user_roles",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_user_role_active",
        columnNames = {"user_id", "role_id"}
    ),
    indexes = {
        @Index(name = "idx_user_role_user", columnList = "user_id"),
        @Index(name = "idx_user_role_role", columnList = "role_id")
    }
)
public class UserRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Date de début de validité. null = immédiatement actif.
     */
    @Column(name = "valid_from")
    private LocalDate validFrom;

    /**
     * Date de fin de validité. null = pas d'expiration.
     * Révocation = setValidTo(LocalDate.now()) sans suppression.
     */
    @Column(name = "valid_to")
    private LocalDate validTo;

    /**
     * Qui a assigné ce rôle (username de l'admin).
     */
    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    /**
     * Raison de l'assignation (obligatoire pour audit en ERP).
     */
    @Column(name = "reason", length = 500)
    private String reason;

    protected UserRole() {}

    public UserRole(User user, Role role, String assignedBy) {
        this.user = user;
        this.role = role;
        this.assignedBy = assignedBy;
        this.validFrom = LocalDate.now();
    }

    /**
     * Un UserRole est actif si :
     * - validFrom est null ou dans le passé
     * - validTo est null ou dans le futur
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        boolean fromOk = validFrom == null || !now.isBefore(validFrom);
        boolean toOk = validTo == null || !now.isAfter(validTo);
        return fromOk && toOk;
    }

    /**
     * Révoque ce rôle en définissant la date de fin à aujourd'hui.
     * Préférable à une suppression physique pour l'audit.
     */
    public void revoke(String revokedBy) {
        this.validTo = LocalDate.now();
        this.reason = "Révoqué par : " + revokedBy;
    }

    // Getters/Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Role getRole() { return role; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public String getAssignedBy() { return assignedBy; }
    public String getReason() { return reason; }
}