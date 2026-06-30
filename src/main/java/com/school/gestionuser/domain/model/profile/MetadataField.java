package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Définition d'un champ dynamique pour un BusinessProfileType.
 *
 * C'est le SCHÉMA d'un champ, pas sa valeur.
 * Ex: Pour STUDENT → studentNumber (TEXT, required=true, unique=true)
 *                  → enrollmentDate (DATE, required=true)
 *                  → class (SELECT, options: [6A, 6B, 5A...])
 *
 * Ces définitions pilotent :
 * 1. La génération du formulaire Angular (fieldType → composant UI)
 * 2. La validation côté serveur (required, regex, min/max)
 * 3. La recherche (searchable=true → index en DB)
 */
@Entity
@Table(name = "metadata_fields", indexes = {
    @Index(name = "idx_mf_profile_type", columnList = "profile_type_id"),
    @Index(name = "idx_mf_field_code", columnList = "field_code, profile_type_id",
           unique = true)
})
public class MetadataField extends BaseEntity {

    /**
     * Code technique du champ. Unique par profileType.
     * Ex: student_number, enrollment_date, subject, employee_id
     * snake_case par convention.
     */
    @Column(name = "field_code", nullable = false, length = 100)
    private String fieldCode;

    @Column(name = "field_label", nullable = false, length = 200)
    private String fieldLabel; // Libellé affiché : "Numéro étudiant"

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false, length = 20)
    private FieldType fieldType;

    @Column(name = "required", nullable = false)
    private boolean required = false;

    @Column(name = "unique_field", nullable = false)
    private boolean unique = false;

    /**
     * Si true, une colonne/index de recherche sera utilisée.
     * Permet l'optimisation des recherches par champ métier spécifique.
     */
    @Column(name = "searchable", nullable = false)
    private boolean searchable = false;

    @Column(name = "display_in_list", nullable = false)
    private boolean displayInList = false;

    @Column(name = "default_value", length = 500)
    private String defaultValue;

    /**
     * Regex de validation côté serveur ET Angular.
     * Ex: "^[A-Z]{2}[0-9]{6}$" pour un numéro étudiant
     */
    @Column(name = "validation_regex", length = 500)
    private String validationRegex;

    @Column(name = "validation_message", length = 300)
    private String validationMessage;

    @Column(name = "placeholder", length = 200)
    private String placeholder;

    @Column(name = "help_text", length = 500)
    private String helpText;

    @Column(name = "min_length")
    private Integer minLength;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    /**
     * Si le champ est conditionnel, dépend d'un autre champ.
     * Ex: field "specialization" visible seulement si "level" = "MASTER"
     * Stocké en JSON : {"dependsOn": "level", "value": "MASTER"}
     */
    @Column(name = "condition_json", length = 500)
    private String conditionJson;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_type_id", nullable = false)
    private BusinessProfileType profileType;

    /**
     * Options pour les champs SELECT.
     * Ordonnées par displayOrder.
     */
    @OneToMany(mappedBy = "metadataField", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("display_order ASC")
    private List<FieldOption> options = new ArrayList<>();

    protected MetadataField() {}

    public MetadataField(String fieldCode, String fieldLabel, FieldType fieldType) {
        this.fieldCode = fieldCode;
        this.fieldLabel = fieldLabel;
        this.fieldType = fieldType;
    }

    // Static factory methods
    public static MetadataField text(String code, String label, boolean required) {
        MetadataField f = new MetadataField(code, label, FieldType.TEXT);
        f.required = required;
        return f;
    }

    public static MetadataField date(String code, String label, boolean required) {
        MetadataField f = new MetadataField(code, label, FieldType.DATE);
        f.required = required;
        return f;
    }

    public static MetadataField select(String code, String label,
                                       List<FieldOption> options) {
        MetadataField f = new MetadataField(code, label, FieldType.SELECT);
        f.required = true;
        f.options.addAll(options);
        options.forEach(o -> o.setMetadataField(f));
        return f;
    }

    public void addOption(FieldOption option) {
        option.setMetadataField(this);
        option.setDisplayOrder(options.size());
        options.add(option);
    }

    // Getters/Setters
    public String getFieldCode() { return fieldCode; }
    public void setFieldCode(String fieldCode) { this.fieldCode = fieldCode; }
    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }
    public FieldType getFieldType() { return fieldType; }
    public void setFieldType(FieldType fieldType) { this.fieldType = fieldType; }
    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }
    public boolean isUnique() { return unique; }
    public void setUnique(boolean unique) { this.unique = unique; }
    public boolean isSearchable() { return searchable; }
    public void setSearchable(boolean searchable) { this.searchable = searchable; }
    public boolean isDisplayInList() { return displayInList; }
    public void setDisplayInList(boolean displayInList) { this.displayInList = displayInList; }
    public String getDefaultValue() { return defaultValue; }
    public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
    public String getValidationRegex() { return validationRegex; }
    public void setValidationRegex(String validationRegex) { this.validationRegex = validationRegex; }
    public String getValidationMessage() { return validationMessage; }
    public void setValidationMessage(String validationMessage) { this.validationMessage = validationMessage; }
    public String getPlaceholder() { return placeholder; }
    public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
    public String getHelpText() { return helpText; }
    public void setHelpText(String helpText) { this.helpText = helpText; }
    public Integer getMinLength() { return minLength; }
    public void setMinLength(Integer minLength) { this.minLength = minLength; }
    public Integer getMaxLength() { return maxLength; }
    public void setMaxLength(Integer maxLength) { this.maxLength = maxLength; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public String getConditionJson() { return conditionJson; }
    public void setConditionJson(String conditionJson) { this.conditionJson = conditionJson; }
    public BusinessProfileType getProfileType() { return profileType; }
    public void setProfileType(BusinessProfileType profileType) { this.profileType = profileType; }
    public List<FieldOption> getOptions() { return options; }
}