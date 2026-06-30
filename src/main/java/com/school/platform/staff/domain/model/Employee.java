package com.school.platform.staff.domain.model;


import com.school.platform.shared.domain.BaseEntity;
import com.school.platform.identityaccess.domain.model.Person;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Employee extends BaseEntity {

    @EqualsAndHashCode.Include
    @OneToOne
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @NotBlank(message = "Le numéro d'employé est obligatoire")
    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String employeeNumber;

    @NotNull(message = "La date d'embauche est obligatoire")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EmployeePosition> positions = new HashSet<>();
}
