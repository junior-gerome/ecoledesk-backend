package com.school.platform.academic.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sections")
public class Section extends BaseEntity {

    @Column(nullable = false)
    private String libelle;

    @Column(name = "description")
    private String description;
}
