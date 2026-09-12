package com.school.platform.billing.application.dto;

import java.math.BigDecimal;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.billing.domain.model.Montant;

/**
 * Représentation sûre d'un montant pour la sérialisation REST.
 * Évite de sérialiser l'entité JPA {@link Montant} (graphe lazy : classeRoom,
 * section, teacher, academicYear) ce qui provoquait des erreurs 500.
 */
public record MontantResponse(
        Long id,
        BigDecimal count,
        String typePaiement,
        ClasseRoomResponse classeRoom) {

    public record ClasseRoomResponse(Long id, String nameClasse, String level, Integer capacity) {

        public static ClasseRoomResponse from(ClasseRoom room) {
            if (room == null) {
                return null;
            }
            return new ClasseRoomResponse(room.getId(), room.getNameClasse(), room.getLevel(), room.getCapacity());
        }
    }

    public static MontantResponse from(Montant montant) {
        if (montant == null) {
            return null;
        }
        return new MontantResponse(
                montant.getId(),
                montant.getCount(),
                montant.getTypePaiement() != null ? montant.getTypePaiement().name() : null,
                ClasseRoomResponse.from(montant.getClasseRoom()));
    }
}