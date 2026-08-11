package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreEnrollmentBasicDTO {
    private Long id;
    private String number;
    private PreEnrollmentStatus status;
    private String applicantFirstName;
    private String applicantLastName;
    private String requestedLevel;
    private Long academicYearId;
}
