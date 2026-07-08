package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import com.school.gestionuser.domain.model.identity.Role;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AGGREGATE ROOT : Définit un type de profil métier (STUDENT, TEACHER, etc.)
 *
 * C'est le SCHÉMA du profil, pas une instance.
 * Il contient la liste des champs dynamiques (MetadataField) qui seront
 * demandés lors de la création d'un profil de ce type.
 *
 * Principe Open/Closed : on peut ajouter PSYCHOLOGIST en DB sans modifier
 * une seule ligne de code Java.
 */
@Entity
@Table(name = "business_profile_types", indexes = {
    @Index(name = "idx_bpt_code", columnList = "code", unique = true),
    @Index(name = "idx_bpt_active", columnList = "active")
})
public class BusinessProfileType extends BaseEntity {

    /**
     * Code technique unique. Ex: STUDENT, TEACHER, PARENT, DIRECTOR, PSYCHOLOGIST
     * MAJUSCULES_UNDERSCORE par convention. Utilisé dans le code pour les switch/cases
     * exceptionnels, et dans les URL REST.
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // Affiché dans l'UI : "Étudiant", "Enseignant"

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "icon_class", length = 100)
    private String iconClass; // Material icon : "school", "person", "account_balance"

    @Column(name = "color_code", length = 20)
    private String colorCode; // Hex : #4CAF50

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "display_order")
    private int displayOrder = 0;

    /**
     * Liste ordonnée des champs dynamiques de ce type de profil.
     * CascadeType.ALL : si on supprime BusinessProfileType, on supprime aussi ses champs.
     * orphanRemoval = true : si on retire un field de la liste, il est supprimé en DB.
     */
    @OneToMany(mappedBy = "profileType", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("display_order ASC")
    private List<MetadataField> fields = new ArrayList<>();

    /**
     * Mapping vers les rôles de sécurité à assigner automatiquement
     * quand ce type de profil est créé pour une Person.
     * Ex: créer un profil STUDENT → assigne automatiquement ROLE_STUDENT
     */
    @OneToMany(mappedBy = "profileType", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ProfileTypeRoleMapping> roleMappings = new HashSet<>();

    protected BusinessProfileType() {}

    public BusinessProfileType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Domain methods
    public void addField(MetadataField field) {
        field.setProfileType(this);
        field.setDisplayOrder(fields.size());
        fields.add(field);
    }

    public void addRoleMapping(ProfileTypeRoleMapping mapping) {
        mapping.setProfileType(this);
        roleMappings.add(mapping);
    }

    public List<Role> getAutoAssignRoles() {
        return roleMappings.stream()
            .filter(ProfileTypeRoleMapping::isAutoAssign)
            .map(ProfileTypeRoleMapping::getRole)
            .toList();
    }

    public boolean isRequired(String fieldCode) {
        return fields.stream()
            .anyMatch(f -> f.getFieldCode().equals(fieldCode) && f.isRequired());
    }

    // Getters/Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconClass() { return iconClass; }
    public void setIconClass(String iconClass) { this.iconClass = iconClass; }
    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public List<MetadataField> getFields() { return fields; }
    public Set<ProfileTypeRoleMapping> getRoleMappings() { return roleMappings; }
}