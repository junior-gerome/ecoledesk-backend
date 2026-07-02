// com.school.gestionuser.domain.port.in.profile.CreateProfileTypeUseCase
package com.school.gestionuser.domain.port.in.profile;

import com.school.gestionuser.application.dto.command.CreateProfileTypeCommand;
import com.school.gestionuser.domain.model.profile.BusinessProfileType;

public interface CreateProfileTypeUseCase {
    /**
     * Crée un nouveau type de profil métier depuis l'UI d'administration.
     * Aucune modification de code requise.
     */
    BusinessProfileType createProfileType(CreateProfileTypeCommand command);
}
