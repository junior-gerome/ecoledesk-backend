package com.school.platform.identityaccess.domain.model;

/**
 * Scope applicatif (domaine fonctionnel) auquel se rattache un role ou une permission.
 * Permet de regrouper et de filtrer les droits par grande famille metier.
 */
public enum PermissionScope {
    SYSTEME("Systeme"),
    IDENTITE("Identite & comptes"),
    SCOLARITE("Scolarite"),
    PEDAGOGIQUE("Pedagogie"),
    FINANCE("Finance"),
    ADMINISTRATION("Administration"),
    COMMUNICATION("Communication");

    private final String label;

    PermissionScope(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Deduit un scope a partir de la ressource d'une permission.
     * Utilise en secours lorsqu'aucun scope n'est fourni explicitement.
     */
    public static PermissionScope fromResource(String resource) {
        if (resource == null || resource.isBlank()) return ADMINISTRATION;
        return switch (resource.trim().toUpperCase().replace('-', '_').replace(' ', '_')) {
            case "USER", "USERS", "ACCOUNT", "ACCOUNTS", "IDENTITY", "IDENTITE", "AUTH", "ROLE", "ROLES",
                 "PERMISSION", "PERMISSIONS" -> IDENTITE;
            case "STUDENT", "STUDENTS", "ENROLLMENT", "ENROLLMENTS", "PRE_ENROLLMENT", "PRE_ENROLLMENTS",
                 "CLASS", "CLASSES", "SECTION", "SECTIONS", "GUARDIAN", "GUARDIANS", "STAFF" -> SCOLARITE;
            case "GRADE", "GRADES", "ATTENDANCE", "EXAM", "EXAMS", "REPORT_CARD", "REPORT_CARDS",
                 "BULLETIN", "BULLETINS" -> PEDAGOGIQUE;
            case "BILLING", "PAYMENT", "PAYMENTS", "INVOICE", "INVOICES", "TUITION" -> FINANCE;
            case "NOTIFICATION", "NOTIFICATIONS", "MESSAGE", "MESSAGES", "ANNOUNCEMENT",
                 "ANNOUNCEMENTS", "DOCUMENT", "DOCUMENTS" -> COMMUNICATION;
            default -> ADMINISTRATION;
        };
    }

    /**
     * Scope par defaut associe a un code de role du referentiel.
     */
    public static PermissionScope fromRoleCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) return ADMINISTRATION;
        String normalized = roleCode.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        if (normalized.startsWith("ROLE_")) normalized = normalized.substring(5);
        return switch (normalized) {
            case "ADMIN", "SUPER_ADMIN", "SUPERADMIN", "ROOT" -> SYSTEME;
            case "DIRECTEUR", "DIRECTOR", "PROVISEUR", "PRINCIPAL" -> ADMINISTRATION;
            case "ENSEIGNANT", "TEACHER", "PROFESSEUR" -> PEDAGOGIQUE;
            case "COMPTABLE", "ACCOUNTANT", "CAISSIER" -> FINANCE;
            case "SECRETAIRE", "SECRETARY", "AGENT", "SURVEILLANT", "PARENT", "ELEVE",
                 "STUDENT", "GUARDIAN" -> SCOLARITE;
            default -> ADMINISTRATION;
        };
    }
}
