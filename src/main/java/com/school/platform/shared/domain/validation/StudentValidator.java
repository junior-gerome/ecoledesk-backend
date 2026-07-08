package com.school.platform.shared.domain.validation;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.school.platform.enrollment.application.dto.StudentDTO;

@Component
public class StudentValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return StudentDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        StudentDTO student = (StudentDTO) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required", "Le prénom est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "field.required", "Le nom est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", "L'email est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "field.required", "Le genre est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateOfBirth", "field.required", "La date de naissance est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "section", "field.required", "La section est obligatoire");

        if (student.getLastNameStudent() != null && student.getLastNameStudent().length() < 2) {
            errors.rejectValue("firstName", "size.tooShort", "Le prénom doit contenir au moins 2 caractères");
        }
        if (student.getFirstNameStudent() != null && student.getFirstNameStudent().length() < 2) {
            errors.rejectValue("lastName", "size.tooShort", "Le nom doit contenir au moins 2 caractères");
        }

        if (student.getDateOfBirth() != null && student.getDateOfBirth().isAfter(java.time.LocalDate.now())) {
            errors.rejectValue("dateOfBirth", "invalid.date", "La date de naissance ne peut pas être dans le futur");
        }
    }
} 