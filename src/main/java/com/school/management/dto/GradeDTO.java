package com.school.management.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.school.management.enums.AssessmentType;
import com.school.management.model.ClasseRoom;
import com.school.management.model.Sequence;
import com.school.management.model.Student;
import com.school.management.model.Subject;
import com.school.management.model.Trimestre;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {
    private Long id;
    private BigDecimal grade;
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
