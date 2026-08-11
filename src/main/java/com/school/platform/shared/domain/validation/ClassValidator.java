package com.school.platform.shared.domain.validation;

import com.school.platform.shared.domain.exception.ValidationException;
import com.school.platform.academic.application.dto.ClasseRoomDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClassValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ClasseRoomDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ClasseRoomDTO classe = (ClasseRoomDTO) target;
        List<String> validationMessages = new ArrayList<>();

        // Validation de base (champs obligatoires)
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nameClasse", "field.required", "Le nom de la classe est obligatoire");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "level", "field.required", "Le niveau est obligatoire");

        // Validation personnalisée

        // Nom de classe
        if (classe.getNameClasse() != null && classe.getNameClasse().trim().length() > 50) {
            errors.rejectValue("nameClasse", "invalid.name", "Le nom de la classe ne doit pas dépasser 50 caractères");
            validationMessages.add("Le nom de la classe ne doit pas dépasser 50 caractères");
        }

        // Capacité
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

        // Année scolaire (ID obligatoire)
        if (classe.getAcademicYear() == null || classe.getAcademicYear().getId() == null) {
            errors.rejectValue("academicYear", "field.required", "L'année scolaire est obligatoire");
            validationMessages.add("L'année scolaire est obligatoire");
        }

        // Description
        if (classe.getDescription() != null && classe.getDescription().length() > 500) {
            errors.rejectValue("description", "invalid.description", "La description ne doit pas dépasser 500 caractères");
            validationMessages.add("La description ne doit pas dépasser 500 caractères");
        }

        // Si des erreurs ont été accumulées, on lève une exception personnalisée
        if (!validationMessages.isEmpty()) {
            throw new ValidationException(validationMessages);
        }
    }
}
