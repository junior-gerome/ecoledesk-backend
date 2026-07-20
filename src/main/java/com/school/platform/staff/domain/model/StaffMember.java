package com.school.platform.staff.domain.model;

import java.time.LocalDate;

import com.school.platform.identityaccess.domain.model.BaseEntity;
import com.school.platform.identityaccess.domain.model.Person;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff_members")
@Getter
@Setter
@NoArgsConstructor
public class StaffMember extends BaseEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    @NotNull
    private Person person;

    @Embedded
    private EmployeeNumber employeeNumber;

    @Column(name = "employment_date", nullable = false)
    private LocalDate employmentDate;
}
