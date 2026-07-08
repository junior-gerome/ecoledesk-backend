package com.school.gestionuser.domain.model.profile;

public enum ProfileStatus {
    ACTIVE,         // Profil actif normal
    INACTIVE,       // Profil désactivé
    SUSPENDED,      // Suspendu temporairement
    GRADUATED,      // Diplômé (pour STUDENT)
    TRANSFERRED,    // Transféré vers un autre établissement
    ON_LEAVE        // En congé (pour TEACHER)
}