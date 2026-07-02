package com.school.platform.academic.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradeResponseDTO {

    private Long id;

    private Long studentId;
    private String studentName;

    private Long subjectId;
    private String subjectName;

    private Long classeId;
    private String classeName;

    private BigDecimal score;
    private BigDecimal coefficient;

    private String period;
    private String comments;

    private LocalDateTime assessmentDate;
}
