package com.school.platform.academic.domain.model;

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
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Table(name = "classes")
public class ClasseRoom extends BaseEntity {

    @Column(name = "name_classe", nullable = false, length = 100)
    private String nameClasse;

    @Column(name = "niveau", nullable = false, length = 50)
    private String level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(name = "capacite")
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_member_id")
    private StaffMember teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @Column(name = "description", length = 500)
    private String description;


    /**
     * Helper accessor for the assigned staff member ID (teacher position).
     */
    public Long getStaffMemberId() {
        return teacher != null ? teacher.getId() : null;
    }
}
