package com.school.platform.enrollment.application.dto.enrollment;

import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.enrollment.EnrollmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentBasicDTO {
    private Long id;
    private String number;
    private EnrollmentStatus status;
    private EnrollmentType type;
    private LocalDate enrollmentDate;
    private Long studentId;
    private String studentName;
    private Long classroomId;
    private String classroomName;
}
