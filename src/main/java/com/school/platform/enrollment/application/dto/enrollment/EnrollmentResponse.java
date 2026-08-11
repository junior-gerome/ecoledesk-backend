package com.school.platform.enrollment.application.dto.enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus; import lombok.Builder; import lombok.Data; @Data @Builder public class EnrollmentResponse { private Long id; private String number; private Long preEnrollmentId; private Long studentId; private Long classroomId; private EnrollmentStatus status; }
