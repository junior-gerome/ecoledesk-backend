package com.school.platform.staff.application.dto.employee;

import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeMediumDTO {
    private Long id;
    private PersonMediumDTO person;
    private String employeeNumber;
    private LocalDate hireDate;
    private Integer activePositionsCount;
}
