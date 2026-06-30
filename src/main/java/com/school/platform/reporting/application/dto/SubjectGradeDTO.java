package com.school.platform.reporting.application.dto;

import lombok.*;

import com.school.platform.academic.domain.model.Grade;
import com.school.platform.academic.domain.model.Sequence;
import com.school.platform.academic.domain.model.Subject;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.academic.domain.model.Trimestre;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGradeDTO {
    private Long id;

    private Subject subject;
    private Sequence sequence;
    private ClasseRoom classe;
    private Trimestre trimestre;
    private Grade grade;

    
}
