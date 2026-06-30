package com.school.platform.enrollment.application.dto.student;

import com.school.platform.enrollment.application.dto.parent.ParentBasicDTO;
import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFullDTO {
    private Long id;
    private PersonFullDTO person;
    private String studentNumber;
    private LocalDate admissionDate;
    private String currentLevel;
    private String ecolePrecedente;
    private List<StudentParentDTO> parents;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentParentDTO {
        private ParentBasicDTO parent;
        private String relationshipType;
    }
}
