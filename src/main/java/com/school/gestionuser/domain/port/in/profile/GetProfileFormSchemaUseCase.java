// com.school.gestionuser.domain.port.in.profile.GetProfileFormSchemaUseCase
package com.school.gestionuser.domain.port.in.profile;

import com.school.gestionuser.application.dto.query.FormSchemaDto;

public interface GetProfileFormSchemaUseCase {
    /**
     * Retourne le schéma de formulaire JSON pour un type de profil.
     * Utilisé par Angular pour générer dynamiquement les formulaires.
     */
    FormSchemaDto getFormSchema(String profileTypeCode);
}
