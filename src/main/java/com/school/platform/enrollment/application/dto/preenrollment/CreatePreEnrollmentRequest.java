package com.school.platform.enrollment.application.dto.preenrollment;

import com.school.platform.enrollment.domain.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreatePreEnrollmentRequest {

    @NotBlank(message = "Le prénom de l'élève est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de l'élève est obligatoire")
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate birthDate;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    private String birthPlace;

    @NotNull(message = "L'année académique est obligatoire")
    private Long academicYearId;

    @NotBlank(message = "Le niveau demandé est obligatoire")
    private String requestedLevel;

    private BigDecimal requiredFee;
}
