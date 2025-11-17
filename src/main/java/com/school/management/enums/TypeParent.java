package com.school.management.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// Enumération pour les types d'évaluation
public enum TypeParent {
  PERE ("PERE") ,
  MERE ("MERE") ,
  TUTEUR_LEGAL ("TUTEUR_LEGAL") ,
  ONCLE ("ONCLE") ,
  TANTE ("TANTE"),
  GRAND_PERE ("GRAND_PERE"),
  GRANDE_MERE ("GRANDE_MERE"),
  FRERE_AINE ("FRERE_AINE"),
  SOEUR_AINE ("SOEUR_AINE");

   private final String value;

    TypeParent (String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TypeParent fromValue(String value) {
        for (TypeParent typeParent : TypeParent.values()) {
            if (typeParent.value.equalsIgnoreCase(value)) {
                return typeParent;
            }
        }
        throw new IllegalArgumentException("Valeur inconnue: " + value);
    }
}

