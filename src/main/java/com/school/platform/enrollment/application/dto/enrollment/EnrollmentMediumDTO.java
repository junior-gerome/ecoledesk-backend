package com.school.platform.enrollment.application.dto.enrollment;

import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.enrollment.EnrollmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentMediumDTO {
    private Long id;
    private String number;
    private EnrollmentStatus status;
    private EnrollmentType type;
    private LocalDate enrollmentDate;
    private LocalDate confirmationDate;
    private StudentBasicDTO student;
    private Long classroomId;
    private String classroomName;
    private Long academicYearId;
    private String academicYearLabel;
}
