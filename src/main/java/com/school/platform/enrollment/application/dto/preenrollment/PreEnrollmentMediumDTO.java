package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.Gender;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreEnrollmentMediumDTO {
    private Long id;
    private String number;
    private PreEnrollmentStatus status;
    private String applicantFirstName;
    private String applicantLastName;
    private LocalDate applicantBirthDate;
    private Gender applicantGender;
    private String applicantBirthPlace;
    private String requestedLevel;
    private Long academicYearId;
    private String academicYearLabel;
    private BigDecimal requiredFee;
    private LocalDateTime submittedAt;
}
