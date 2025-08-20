package com.school.validator;

import com.school.exception.ValidationException;
import com.school.management.dto.ClassDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ClassValidator implements Validator {

    private static final Pattern ACADEMIC_YEAR_PATTERN = Pattern.compile("^\\d{4}-\\d{4}$");

    @Override
    public boolean supports(Class<?> clazz) {
        return ClassDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ClassDTO classe = (ClassDTO) target;
        List<String> validationMessages = new ArrayList<>();

        // Validation Spring : champs requis
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Le nom de la classe est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "level", "field.required", "Le niveau est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "academicYear", "field.required", "L'année académique est obligatoire");

        // Validation personnalisée

        // Name length
        if (classe.getNameClasse() != null && classe.getNameClasse().trim().length() > 50) {
            errors.rejectValue("name", "invalid.name", "Le nom de la classe ne doit pas dépasser 50 caractères");
            validationMessages.add("Le nom de la classe ne doit pas dépasser 50 caractères");
        }

        // Capacity
        if (classe.getCapacity() == null) {
            errors.rejectValue("capacity", "field.required", "La capacité est requise");
            validationMessages.add("La capacité est requise");
        } else if (classe.getCapacity() < 1) {
            errors.rejectValue("capacity", "invalid.capacity", "La capacité doit être supérieure à 0");
            validationMessages.add("La capacité doit être supérieure à 0");
        } else if (classe.getCapacity() > 100) {
            errors.rejectValue("capacity", "invalid.capacity.max", "La capacité ne peut pas dépasser 100 élèves");
            validationMessages.add("La capacité ne peut pas dépasser 100 élèves");
        }

        // Academic year format
        if (classe.getAnneeScolaire().getLibelleAnneeScolaire() != null && !ACADEMIC_YEAR_PATTERN.matcher(classe.getAnneeScolaire().getLibelleAnneeScolaire().trim()).matches()) {
            errors.rejectValue("academicYear", "invalid.academicYear", "Le format de l'année scolaire doit être YYYY-YYYY");
            validationMessages.add("Le format de l'année scolaire doit être YYYY-YYYY");
        }

        // Description length
        if (classe.getDescription() != null && classe.getDescription().length() > 500) {
            errors.rejectValue("description", "invalid.description", "La description ne doit pas dépasser 500 caractères");
            validationMessages.add("La description ne doit pas dépasser 500 caractères");
        }

        // Levée d’exception personnalisée pour d'autres couches (REST, service, etc.)
        if (!validationMessages.isEmpty()) {
            throw new ValidationException(validationMessages);
        }
    }
}
