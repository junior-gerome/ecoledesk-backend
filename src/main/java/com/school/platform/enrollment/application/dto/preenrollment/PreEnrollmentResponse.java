package com.school.platform.enrollment.application.dto.preenrollment;

import java.time.LocalDateTime; 
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus; import lombok.*;

@Data @Builder public class PreEnrollmentResponse { 
  private Long id; 
  private String number; 
  private PreEnrollmentStatus status; 
  private Long academicYearId; 
  private String requestedLevel; 
  private LocalDateTime submittedAt; 
}
