package com.school.platform.enrollment.application.dto.student;

import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentMediumDTO {
    private Long id;
    private PersonMediumDTO person;
    private String studentNumber;
    private LocalDate admissionDate;
    private String currentLevel;
    private String ecolePrecedente;
    private Boolean active;
}
