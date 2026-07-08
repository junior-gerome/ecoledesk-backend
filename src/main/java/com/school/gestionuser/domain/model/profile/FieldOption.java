package com.school.gestionuser.domain.model.profile;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;

/**
 * Option pour un champ SELECT.
 * Ex: Pour le champ "class" de STUDENT :
 * options = [{value:"6A", label:"6ème A"}, {value:"6B", label:"6ème B"}, ...]
 */
@Entity
@Table(name = "field_options", indexes = {
    @Index(name = "idx_fo_field", columnList = "metadata_field_id")
})
public class FieldOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metadata_field_id", nullable = false)
    private MetadataField metadataField;

    @Column(name = "option_value", nullable = false, length = 200)
    private String optionValue; // Valeur technique stockée

    @Column(name = "option_label", nullable = false, length = 200)
    private String optionLabel; // Libellé affiché dans l'UI

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected FieldOption() {}

    public FieldOption(String optionValue, String optionLabel) {
        this.optionValue = optionValue;
        this.optionLabel = optionLabel;
    }

    public MetadataField getMetadataField() { return metadataField; }
    public void setMetadataField(MetadataField metadataField) { this.metadataField = metadataField; }
    public String getOptionValue() { return optionValue; }
    public void setOptionValue(String optionValue) { this.optionValue = optionValue; }
    public String getOptionLabel() { return optionLabel; }
    public void setOptionLabel(String optionLabel) { this.optionLabel = optionLabel; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}