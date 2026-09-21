package com.school.platform.identityaccess.domain.model;

/**
 * Cycle de vie d'un compte utilisateur.
 *
 * <pre>
 *   PENDING ──▶ ACTIVE ──▶ SUSPENDED
 *      │            ▲            │
 *      └────────────┴────────────┘
 *        (PENDING -> SUSPENDED, SUSPENDED -> ACTIVE)
 * </pre>
 */
public enum UserAccountStatus {
    PENDING,
    ACTIVE,
    SUSPENDED;

    /**
     * Indique si le compte peut se connecter. Seul ACTIVE autorise la connexion.
     */
    public boolean allowsLogin() {
        return this == ACTIVE;
    }

    /**
     * Verifie qu'une transition d'etat est autorisee.
     * L'etat cible identique est tolere (idempotence).
     */
    public boolean canTransitionTo(UserAccountStatus target) {
        if (target == null) return false;
        if (this == target) return true;
        return switch (this) {
            case PENDING -> target == ACTIVE || target == SUSPENDED;
            case ACTIVE -> target == SUSPENDED;
            case SUSPENDED -> target == ACTIVE;
        };
    }
}
