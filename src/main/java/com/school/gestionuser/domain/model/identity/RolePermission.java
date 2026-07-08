package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Entité de jointure Role ↔ Permission.
 * Utilisation d'une entité pour stocker les métadonnées d'audit.
 */
@Entity
@Table(name = "role_permissions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_role_permission",
        columnNames = {"role_id", "permission_id"}
    )
)
public class RolePermission extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "permission_id", nullable = false)
    private Permission permission;

    @Column(name = "granted_by", length = 100)
    private String grantedBy;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    protected RolePermission() {}

    public RolePermission(Role role, Permission permission) {
        this.role = role;
        this.permission = permission;
        this.grantedAt = LocalDateTime.now();
    }

    public Role getRole() { return role; }
    public Permission getPermission() { return permission; }
    public String getGrantedBy() { return grantedBy; }
    public void setGrantedBy(String grantedBy) { this.grantedBy = grantedBy; }
    public LocalDateTime getGrantedAt() { return grantedAt; }
}