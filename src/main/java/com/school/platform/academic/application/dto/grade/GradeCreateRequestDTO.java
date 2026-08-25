package com.school.platform.academic.application.dto.grade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GradeCreateRequestDTO {

    private Long studentId;
    private Long subjectId;
    private Long classeId;
    private Long sequenceId;
    private Long trimestreId;

    private BigDecimal score;
    private BigDecimal coefficient;

    private String period;
    private String comments;

    private LocalDateTime assessmentDate;
}
