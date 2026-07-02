package com.school.gestionuser.domain.service;

import com.school.gestionuser.domain.model.identity.*;
import com.school.gestionuser.domain.model.profile.*;
import com.school.gestionuser.domain.port.out.*;
import com.school.gestionuser.domain.exception.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service Domaine : Orchestre l'assignation d'un profil métier.
 *
 * Responsabilités :
 * 1. Valider que le profil n'existe pas déjà (sauf si autorisé)
 * 2. Valider les champs obligatoires
 * 3. Créer le BusinessProfile avec ses valeurs
 * 4. Assigner automatiquement les rôles configurés
 */
@Service
public class ProfileAssignmentDomainService {

    private final BusinessProfileRepositoryPort profileRepo;
    private final BusinessProfileTypeRepositoryPort profileTypeRepo;
    private final MetadataFieldRepositoryPort metadataFieldRepo;
    private final RoleRepositoryPort roleRepo;
    private final UserRepositoryPort userRepo;

    public ProfileAssignmentDomainService(
            BusinessProfileRepositoryPort profileRepo,
            BusinessProfileTypeRepositoryPort profileTypeRepo,
            MetadataFieldRepositoryPort metadataFieldRepo,
            RoleRepositoryPort roleRepo,
            UserRepositoryPort userRepo) {
        this.profileRepo = profileRepo;
        this.profileTypeRepo = profileTypeRepo;
        this.metadataFieldRepo = metadataFieldRepo;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
    }

    /**
     * Crée et valide un BusinessProfile.
     *
     * @param person          La personne concernée
     * @param profileTypeCode Le code du type de profil (ex: "STUDENT")
     * @param fieldValues     Map<fieldCode, value> des valeurs des champs
     * @param assignedBy      Username de l'opérateur
     * @return Le BusinessProfile créé et persisté
     */
    public BusinessProfile assignProfile(Person person,
                                         String profileTypeCode,
                                         Map<String, String> fieldValues,
                                         String assignedBy) {

        // 1. Charger le type de profil
        BusinessProfileType profileType = profileTypeRepo
            .findByCode(profileTypeCode)
            .orElseThrow(() -> new EntityNotFoundException(
                "BusinessProfileType not found: " + profileTypeCode));

        // 2. Vérifier qu'il n'existe pas déjà un profil actif du même type
        if (profileRepo.existsByPersonIdAndProfileTypeCode(
                person.getId(), profileTypeCode)) {
            throw new DuplicateProfileException(
                "Person already has an active profile of type: " + profileTypeCode);
        }

        // 3. Valider les champs obligatoires
        validateRequiredFields(profileType, fieldValues);

        // 4. Créer le BusinessProfile
        BusinessProfile profile = new BusinessProfile(person, profileType);

        // 5. Renseigner les valeurs des champs
        List<MetadataField> fields = profileType.getFields();
        for (MetadataField field : fields) {
            String value = fieldValues.get(field.getFieldCode());
            if (value != null) {
                validateFieldValue(field, value);
                profile.setFieldValue(field, value);
            } else if (field.getDefaultValue() != null) {
                profile.setFieldValue(field, field.getDefaultValue());
            }
        }

        // 6. Persister le profil
        BusinessProfile saved = profileRepo.save(profile);

        // 7. Assigner automatiquement les rôles configurés
        autoAssignRoles(person, profileType, assignedBy);

        return saved;
    }

    private void validateRequiredFields(BusinessProfileType profileType,
                                        Map<String, String> fieldValues) {
        List<String> missingFields = profileType.getFields().stream()
            .filter(MetadataField::isRequired)
            .filter(f -> !fieldValues.containsKey(f.getFieldCode())
                      || fieldValues.get(f.getFieldCode()).isBlank())
            .map(MetadataField::getFieldLabel)
            .toList();

        if (!missingFields.isEmpty()) {
            throw new ValidationException(
                "Champs obligatoires manquants : " + String.join(", ", missingFields));
        }
    }

    private void validateFieldValue(MetadataField field, String value) {
        if (field.getValidationRegex() != null && !value.isBlank()
                && !value.matches(field.getValidationRegex())) {
            throw new ValidationException(
                "Valeur invalide pour le champ '" + field.getFieldLabel()
                + "' : " + (field.getValidationMessage() != null
                    ? field.getValidationMessage() : "format incorrect"));
        }
    }

    private void autoAssignRoles(Person person,
                                  BusinessProfileType profileType,
                                  String assignedBy) {
        List<Role> rolesToAssign = profileType.getAutoAssignRoles();
        // Solution avec le repository
        userRepo.findByPersonId(person.getId()).ifPresent(user -> {
            for (Role role : rolesToAssign) {
                boolean alreadyHasRole = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getCode().equals(role.getCode())
                               && ur.isActive());
                if (!alreadyHasRole) {
                    UserRole userRole = new UserRole(user, role, assignedBy);
                    user.addUserRole(userRole);
                    userRepo.save(user);
                }
            }
        });
    }
}