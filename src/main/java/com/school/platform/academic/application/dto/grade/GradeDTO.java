package com.school.platform.academic.application.dto.grade;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Sequence;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.academic.domain.model.Trimestre;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    private Long id;
    private BigDecimal grade;
    private String period;
    private Student student;
    private Subject subject;
    private ClasseRoom classe;
    //private AssessmentType assessmentType;
    private BigDecimal coefficient;
    private Sequence sequence;
    private Trimestre trimestre;
    private String comments;
    private LocalDateTime assessmentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Double score;

    
}
