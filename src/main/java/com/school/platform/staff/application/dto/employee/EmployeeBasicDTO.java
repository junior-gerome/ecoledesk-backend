package com.school.platform.staff.application.dto.employee;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBasicDTO {
    private Long id;
    private PersonBasicDTO person;
    private String employeeNumber;
}
