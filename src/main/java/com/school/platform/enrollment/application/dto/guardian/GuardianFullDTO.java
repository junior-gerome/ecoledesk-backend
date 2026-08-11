package com.school.platform.enrollment.application.dto.guardian;

import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardianFullDTO {
    private Long id;
    private PersonFullDTO person;
    private String occupation;
    private List<GuardianStudentDTO> students;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardianStudentDTO {
        private StudentBasicDTO student;
        private String relationshipType;
    }
}
