package com.school.platform.academic.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "subject")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = false)
public class Subject extends BaseEntity {

    @Column(name = "name_subject", nullable = false, length = 100)
    private String nameSubject;

    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "coefficient")
    private Integer coefficient;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
}