package com.school.platform.shared.domain.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.school.platform.academic.application.dto.grade.GradeDTO;

@Component
public class GradeValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return GradeDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        GradeDTO grade = (GradeDTO) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "studentId", "field.required", "L'identifiant de l'élève est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "subject", "field.required", "La matière est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "examType", "field.required", "Le type d'examen est obligatoire");

        if (grade.getScore() != null && (grade.getScore() < 0 || grade.getScore() > 20)) {
            errors.rejectValue("score", "invalid.score", "La note doit être comprise entre 0 et 20");
        }

        // if (grade.get != null && grade.getExamDate().isAfter(java.time.LocalDate.now())) {
        //     errors.rejectValue("examDate", "invalid.examDate", "La date d'examen ne peut pas être dans le futur");
        // }
    }
} 