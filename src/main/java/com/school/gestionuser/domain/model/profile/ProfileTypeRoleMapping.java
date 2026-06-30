package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import com.school.gestionuser.domain.model.identity.Role;
import jakarta.persistence.*;

/**
 * Mapping entre un BusinessProfileType et un Role de sécurité.
 *
 * autoAssign = true → quand un profil STUDENT est créé pour une Person,
 * le rôle ROLE_STUDENT est automatiquement assigné au User de cette Person.
 *
 * Cela évite de devoir gérer manuellement les rôles Spring Security
 * lors de la création d'un profil métier.
 *
 * Ex:
 * STUDENT → ROLE_STUDENT (autoAssign: true)
 * TEACHER → ROLE_TEACHER (autoAssign: true)
 * DIRECTOR → ROLE_DIRECTOR (autoAssign: true)
 *                         + ROLE_TEACHER (autoAssign: false, optionnel)
 */
@Entity
@Table(name = "profile_type_role_mappings",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_ptrm_type_role",
        columnNames = {"profile_type_id", "role_id"}
    )
)
public class ProfileTypeRoleMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_type_id", nullable = false)
    private BusinessProfileType profileType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * Si true, le rôle est automatiquement assigné lors de la création du profil.
     * Si false, le mapping est informatif (suggestion) mais pas automatique.
     */
    @Column(name = "auto_assign", nullable = false)
    private boolean autoAssign = true;

    protected ProfileTypeRoleMapping() {}

    public ProfileTypeRoleMapping(BusinessProfileType profileType,
                                  Role role, boolean autoAssign) {
        this.profileType = profileType;
        this.role = role;
        this.autoAssign = autoAssign;
    }

    public BusinessProfileType getProfileType() { return profileType; }
    public void setProfileType(BusinessProfileType profileType) { this.profileType = profileType; }
    public Role getRole() { return role; }
    public boolean isAutoAssign() { return autoAssign; }
    public void setAutoAssign(boolean autoAssign) { this.autoAssign = autoAssign; }
}