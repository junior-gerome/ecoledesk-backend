package com.school.validator;

import com.school.exception.ValidationException;
import com.school.management.dto.TeacherDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class EnseignantValidator implements Validator {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s'-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @Override
    public boolean supports(Class<?> clazz) {
        return TeacherDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        TeacherDTO teacher = (TeacherDTO) target;

        // Validations Spring (null ou vide)
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nom", "field.required", "Le prénom est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "prenom", "field.required", "Le nom est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", "L'email est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "telephone", "field.required", "Le numéro de téléphone est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "specialite", "field.required", "La spécialité est obligatoire");

        // Validation personnalisée
        List<String> messages = new ArrayList<>();

        if (teacher.getLastnameTeacher() != null && !NAME_PATTERN.matcher(teacher.getLastnameTeacher().trim()).matches()) {
            errors.rejectValue("nom", "invalid.nom", "Le prénom contient des caractères invalides");
            messages.add("Le prénom contient des caractères invalides");
        }

        if (teacher.getFirstnameTeacher() != null && !NAME_PATTERN.matcher(teacher.getFirstnameTeacher().trim()).matches()) {
            errors.rejectValue("prenom", "invalid.prenom", "Le nom contient des caractères invalides");
            messages.add("Le nom contient des caractères invalides");
        }

        if (teacher.getEmail() != null && !EMAIL_PATTERN.matcher(teacher.getEmail().trim()).matches()) {
            errors.rejectValue("email", "invalid.email", "Format d'email invalide");
            messages.add("L'adresse email est invalide");
        }

       if (teacher.getPhoneNumber() != null && !PHONE_PATTERN.matcher(teacher.getPhoneNumber().trim()).matches()) {
            errors.rejectValue("telephone", "invalid.telephone", "Format de numéro de téléphone invalide");
            messages.add("Le numéro de téléphone est invalide");
        }

        if (teacher.getSpeciality() != null && teacher.getSpeciality().trim().length() > 100) {
            errors.rejectValue("specialite", "invalid.specialite", "La spécialité ne doit pas dépasser 100 caractères");
            messages.add("La spécialité ne doit pas dépasser 100 caractères");
        }

        if (!messages.isEmpty()) {
            throw new ValidationException(messages);
        }
    }
}
