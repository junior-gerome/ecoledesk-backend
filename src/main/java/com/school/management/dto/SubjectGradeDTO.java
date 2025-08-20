package com.school.management.dto;

import lombok.*;

import com.school.management.model.Grade;
import com.school.management.model.Sequence;
import com.school.management.model.Subject;
import com.school.management.model.Classe;
import com.school.management.model.Trimestre;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubjectGradeDTO {
    private Long id;

    private Subject subject;
    private Sequence sequence;
    private Classe classe;
    private Trimestre trimestre;
    private Grade grade;

    
}
