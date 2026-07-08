package com.school.gestionuser.domain.model.profile;

/**
 * Types de champs supportés pour la génération dynamique des formulaires.
 * Utilisé côté Angular pour le rendu du bon composant UI.
 */
public enum FieldType {
    TEXT,           // <input type="text">
    NUMBER,         // <input type="number">
    DECIMAL,        // <input type="number" step="0.01">
    DATE,           // <mat-datepicker>
    DATETIME,       // <input type="datetime-local">
    EMAIL,          // <input type="email">
    PHONE,          // <input type="tel">
    BOOLEAN,        // <mat-checkbox> ou <mat-toggle>
    SELECT,         // <mat-select> avec options statiques
    MULTI_SELECT,   // <mat-select multiple>
    TEXTAREA,       // <textarea>
    FILE,           // <input type="file">
    URL             // <input type="url">
}