package com.school.platform.enrollment.application.dto;

import java.time.LocalDateTime;

import com.school.platform.enrollment.domain.model.PreinscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PreinscriptionStatusResponse {
    private Long id;
    private Long studentId;
    private PreinscriptionStatus status;
    private String reason;
    private Long changedBy;
    private LocalDateTime changedAt;
}
