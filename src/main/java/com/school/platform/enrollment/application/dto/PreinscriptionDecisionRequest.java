package com.school.platform.enrollment.application.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PreinscriptionDecisionRequest {
    @Size(max = 500, message = "La justification ne doit pas depasser 500 caracteres")
    private String justification;
}
