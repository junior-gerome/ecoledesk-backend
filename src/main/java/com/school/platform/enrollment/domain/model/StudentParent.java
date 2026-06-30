package com.school.platform.enrollment.domain.model;

import com.school.platform.shared.domain.BaseEntity;

import com.school.platform.enrollment.domain.model.RelationshipType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_parents")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class StudentParent extends BaseEntity {

    @EqualsAndHashCode.Include
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @EqualsAndHashCode.Include
    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 20)
    private RelationshipType relationshipType;
}
