package com.school.platform.enrollment.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "student_guardians",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_guardians_student_guardian", columnNames = {"student_id", "guardian_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class StudentGuardian extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "guardian_id", nullable = false)
    @NotNull
    private Guardian guardian;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    @NotNull
    private RelationshipType relationshipType;

    @Column(name = "relationship_details", length = 255)
    private String relationshipDetails;

    @Column(name = "primary_contact", nullable = false)
    private boolean primaryContact;

    @Column(name = "financial_responsible", nullable = false)
    private boolean financialResponsible;

    @Column(name = "emergency_contact", nullable = false)
    private boolean emergencyContact;
}