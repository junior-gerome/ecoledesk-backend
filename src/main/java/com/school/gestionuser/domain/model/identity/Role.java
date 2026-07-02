package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Rôle de sécurité Spring Security.
 * Convention de nommage : ROLE_XXX (ex: ROLE_STUDENT, ROLE_TEACHER, ROLE_DIRECTOR)
 *
 * Les rôles sont stockés en base → modifiables sans redéploiement.
 * systemRole = true → ne peut pas être supprimé via l'UI (protège les rôles critiques).
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_code", columnList = "code", unique = true)
})
public class Role extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code; // Ex: ROLE_STUDENT, ROLE_TEACHER, ROLE_ADMIN

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Ex: "Étudiant", "Enseignant", "Administrateur"

    @Column(name = "description", length = 500)
    private String description;

    /**
     * Un rôle système ne peut pas être supprimé depuis l'UI.
     * Ex: ROLE_ADMIN, ROLE_SYSTEM ne doivent jamais être supprimés.
     */
    @Column(name = "system_role", nullable = false)
    private boolean systemRole = false;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private Set<UserRole> userRoles = new HashSet<>();

    protected Role() {}

    public Role(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public void addPermission(Permission permission) {
        RolePermission rp = new RolePermission(this, permission);
        rolePermissions.add(rp);
    }

    public boolean hasPermission(String permissionCode) {
        return rolePermissions.stream()
            .anyMatch(rp -> rp.getPermission().getCode().equals(permissionCode));
    }

    // Getters/Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isSystemRole() { return systemRole; }
    public void setSystemRole(boolean systemRole) { this.systemRole = systemRole; }
    public Set<RolePermission> getRolePermissions() { return rolePermissions; }
    public Set<UserRole> getUserRoles() { return userRoles; }
}