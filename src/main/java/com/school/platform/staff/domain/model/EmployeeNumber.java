package com.school.platform.staff.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class EmployeeNumber {

    @NotBlank
    @Column(name = "employee_number", nullable = false, unique = true, length = 50)
    private String value;
}
