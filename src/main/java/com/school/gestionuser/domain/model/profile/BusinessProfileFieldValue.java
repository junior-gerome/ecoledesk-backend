package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

/**
 * Valeur d'un champ dynamique pour un BusinessProfile.
 *
 * C'est la VALEUR d'une instance de MetadataField pour un BusinessProfile spécifique.
 *
 * Pattern EAV (Entity-Attribute-Value) contrôlé :
 * - Entity = BusinessProfile
 * - Attribute = MetadataField (fieldCode)
 * - Value = fieldValue (TEXT toujours en DB, typé côté service)
 *
 * Pourquoi TEXT pour fieldValue ?
 * - Flexibilité maximale
 * - Conversion vers le bon type Java au niveau du service (pas en DB)
 * - Évite les colonnes nullables en cascade pour chaque type
 *
 * Contrainte d'unicité : un profil ne peut avoir qu'une seule valeur par champ.
 */
@Entity
@Table(name = "business_profile_field_values",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_profile_field",
        columnNames = {"business_profile_id", "metadata_field_id"}
    ),
    indexes = {
        @Index(name = "idx_bpfv_profile", columnList = "business_profile_id"),
        @Index(name = "idx_bpfv_field", columnList = "metadata_field_id")
    }
)
public class BusinessProfileFieldValue extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_profile_id", nullable = false)
    private BusinessProfile businessProfile;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "metadata_field_id", nullable = false)
    private MetadataField metadataField;

    /**
     * Valeur stockée en TEXT.
     * Interprétation selon metadataField.fieldType :
     * - TEXT/EMAIL/PHONE/URL → String
     * - NUMBER → Integer/Long.parseLong()
     * - DECIMAL → BigDecimal
     * - DATE → LocalDate.parse()
     * - BOOLEAN → Boolean.parseBoolean()
     * - SELECT → optionValue (référence vers FieldOption.optionValue)
     */
    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    protected BusinessProfileFieldValue() {}

    public BusinessProfileFieldValue(BusinessProfile businessProfile,
                                     MetadataField metadataField,
                                     String fieldValue) {
        this.businessProfile = businessProfile;
        this.metadataField = metadataField;
        this.fieldValue = fieldValue;
    }

    public BusinessProfile getBusinessProfile() { return businessProfile; }
    public MetadataField getMetadataField() { return metadataField; }
    public String getFieldValue() { return fieldValue; }
    public void setFieldValue(String fieldValue) { this.fieldValue = fieldValue; }
}