package com.school.platform.enrollment.application.dto.preenrollment;
import java.math.BigDecimal; import java.time.LocalDate; import com.school.platform.enrollment.domain.model.Gender; import lombok.Data;
@Data public class CreatePreEnrollmentRequest { private String firstName; private String lastName; private LocalDate birthDate; private Gender gender; private String birthPlace; private Long academicYearId; private String requestedLevel; private BigDecimal requiredFee; }
