package com.school.platform.enrollment.application.dto.enrollment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEnrollmentFromPreEnrollmentRequest {

    @NotNull(message = "La classe d'affectation est obligatoire")
    private Long classroomId;
}