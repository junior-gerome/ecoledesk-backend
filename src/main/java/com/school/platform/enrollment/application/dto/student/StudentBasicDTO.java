package com.school.platform.enrollment.application.dto.student;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentBasicDTO {
    private Long id;
    private PersonBasicDTO person;
    private String studentNumber;
    private String currentLevel;
    private Boolean active;
}
