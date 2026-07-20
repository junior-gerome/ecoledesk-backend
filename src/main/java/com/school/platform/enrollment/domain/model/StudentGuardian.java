package com.school.platform.enrollment.domain.model;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.identityaccess.domain.model.Person;

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
        uniqueConstraints = @UniqueConstraint(name = "uk_student_guardians_student_person", columnNames = {"student_id", "guardian_person_id"})
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
    @JoinColumn(name = "guardian_person_id", nullable = false)
    @NotNull
    private Person guardianPerson;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    @NotNull
    private RelationshipType relationshipType;
}