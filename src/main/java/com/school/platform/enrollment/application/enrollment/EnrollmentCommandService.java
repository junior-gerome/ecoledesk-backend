package com.school.platform.enrollment.application.enrollment;

import com.school.platform.enrollment.application.dto.enrollment.CreateEnrollmentFromPreEnrollmentRequest;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentResponse;

public interface EnrollmentCommandService {

    EnrollmentResponse createFromApprovedPreEnrollment(Long preEnrollmentId, CreateEnrollmentFromPreEnrollmentRequest request);

    EnrollmentResponse confirm(Long enrollmentId);

    EnrollmentResponse cancel(Long enrollmentId, String reason);

    EnrollmentResponse withdraw(Long enrollmentId);
}
