package com.school.platform.enrollment.application.dto.parent;

import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentFullDTO {
    private Long id;
    private PersonFullDTO person;
    private String occupation;
    private List<ParentStudentDTO> children;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParentStudentDTO {
        private StudentBasicDTO student;
        private String relationshipType;
    }
}
