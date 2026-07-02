package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import com.school.gestionuser.domain.model.identity.Person;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.*;

/**
 * AGGREGATE ROOT : Instance d'un profil métier pour une Person.
 *
 * C'est l'INSTANCE du profil, pas le schéma.
 * Ex: Jean Dupont (Person) a un BusinessProfile de type STUDENT
 *     avec les valeurs : studentNumber="STU-2024-001", class="6A"
 *
 * Une Person peut avoir plusieurs BusinessProfile simultanément :
 * - Marie Martin est à la fois TEACHER et PARENT
 * - Ahmed Bello est TEACHER et DIRECTOR (promotion sans modification de code)
 *
 * Le statut permet de gérer le cycle de vie :
 * ACTIVE → SUSPENDED → INACTIVE → GRADUATED (pour STUDENT)
 */
@Entity
@Table(name = "business_profiles",
    indexes = {
        @Index(name = "idx_bp_person", columnList = "person_id"),
        @Index(name = "idx_bp_type", columnList = "profile_type_id"),
        @Index(name = "idx_bp_active", columnList = "active")
    }
)
public class BusinessProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "profile_type_id", nullable = false)
    private BusinessProfileType profileType;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProfileStatus status = ProfileStatus.ACTIVE;

    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * Valeurs des champs dynamiques pour ce profil.
     * C'est le cœur du pattern EAV contrôlé.
     * Map<fieldCode, BusinessProfileFieldValue> pour accès O(1)
     */
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @MapKey(name = "metadataField.fieldCode")
    private Map<String, BusinessProfileFieldValue> fieldValues = new HashMap<>();

    protected BusinessProfile() {}

    public BusinessProfile(Person person, BusinessProfileType profileType) {
        this.person = person;
        this.profileType = profileType;
        this.validFrom = LocalDate.now();
    }

    // === Domain Methods ===

    /**
     * Récupère la valeur d'un champ par son code.
     * Retourne Optional.empty() si le champ n'existe pas ou n'est pas renseigné.
     */
    public Optional<String> getFieldValue(String fieldCode) {
        BusinessProfileFieldValue fv = fieldValues.get(fieldCode);
        return fv != null ? Optional.ofNullable(fv.getFieldValue()) : Optional.empty();
    }

    /**
     * Définit ou met à jour la valeur d'un champ.
     * Si la valeur n'existe pas, crée un nouveau BusinessProfileFieldValue.
     */
    public void setFieldValue(MetadataField field, String value) {
        BusinessProfileFieldValue existing = fieldValues.get(field.getFieldCode());
        if (existing != null) {
            existing.setFieldValue(value);
        } else {
            BusinessProfileFieldValue newValue =
                new BusinessProfileFieldValue(this, field, value);
            fieldValues.put(field.getFieldCode(), newValue);
        }
    }

    /**
     * Récupère toutes les valeurs sous forme de Map<fieldCode, value>.
     * Utile pour la sérialisation JSON.
     */
    public Map<String, String> getAllFieldValuesAsMap() {
        Map<String, String> result = new HashMap<>();
        fieldValues.forEach((code, fv) -> result.put(code, fv.getFieldValue()));
        return result;
    }

    public void deactivate() {
        this.active = false;
        this.validTo = LocalDate.now();
    }

    public void suspend(String reason) {
        this.status = ProfileStatus.SUSPENDED;
        this.notes = reason;
    }

    public boolean isActive() {
        if (!active) return false;
        LocalDate now = LocalDate.now();
        boolean fromOk = validFrom == null || !now.isBefore(validFrom);
        boolean toOk = validTo == null || !now.isAfter(validTo);
        return fromOk && toOk && status != ProfileStatus.INACTIVE;
    }

    // Getters/Setters
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public BusinessProfileType getProfileType() { return profileType; }
    public void setProfileType(BusinessProfileType profileType) { this.profileType = profileType; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public ProfileStatus getStatus() { return status; }
    public void setStatus(ProfileStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, BusinessProfileFieldValue> getFieldValues() { return fieldValues; }
}