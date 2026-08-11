package com.school.platform.academic.domain.model;

import java.time.LocalDateTime;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.staff.domain.model.StaffMember;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(exclude = {"section", "teacher", "academicYear"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "classes")
public class ClasseRoom extends BaseEntity {
    // @Id
    // @EqualsAndHashCode.Include
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    @Column(nullable = false, length = 100)
    private String nameClasse;

    @Column(name = "niveau", nullable = false, length = 50)
    private String level;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "capacite")
    private Integer capacity;

     @ManyToOne
    @JoinColumn(name = "staff_member_id")
    private StaffMember teacher;

    @ManyToOne
    @JoinColumn(name = "academicYear_id")
    private AcademicYear academicYear;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
