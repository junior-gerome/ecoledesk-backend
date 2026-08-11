package com.school.platform.enrollment.application.dto.enrollment;

import com.school.platform.enrollment.application.dto.student.StudentFullDTO;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.enrollment.EnrollmentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentFullDTO {
    private Long id;
    private String number;
    private EnrollmentStatus status;
    private EnrollmentType type;
    private LocalDate enrollmentDate;
    private LocalDate confirmationDate;
    private LocalDate withdrawalDate;
    private String cancellationReason;
    private StudentFullDTO student;
    private Long preEnrollmentId;
    private String preEnrollmentNumber;
    private Long classroomId;
    private String classroomName;
    private Long academicYearId;
    private String academicYearLabel;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
