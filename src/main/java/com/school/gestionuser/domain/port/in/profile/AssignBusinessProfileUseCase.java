// com.school.gestionuser.domain.port.in.profile.AssignBusinessProfileUseCase
package com.school.gestionuser.domain.port.in.profile;

import com.school.gestionuser.application.dto.command.AssignProfileCommand;
import com.school.gestionuser.domain.model.profile.BusinessProfile;

public interface AssignBusinessProfileUseCase {
    /**
     * Assigne un profil métier à une personne.
     * Valide les champs obligatoires.
     * Assigne automatiquement les rôles configurés (autoAssign).
     */
    BusinessProfile assignProfile(AssignProfileCommand command);
}
