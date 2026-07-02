package com.school.platform.billing.domain.policy;

import com.school.platform.shared.domain.exception.ValidationException;
import com.school.platform.billing.application.dto.PaymentDTO;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaymentValidator {

    public void validatePayment(PaymentDTO payment) {
        List<String> errors = new ArrayList<>();

        // Validate student ID
        if (payment.getId() == null) {
            errors.add("L'identifiant de l'élève est requis");
        }

        // Validate amount
        // if (payment.getMontant() == null) {
        //     errors.add("Le montant est requis");
        // } else if (payment.getMontant().compareTo(BigDecimal.ZERO) <= 0) {
        //     errors.add("Le montant doit être supérieur à 0");
        // }

        // Validate payment date
        if (payment.getDatePaiement() == null) {
            errors.add("La date de paiement est requise");
        } else if (payment.getDatePaiement().isAfter(LocalDate.now())) {
            errors.add("La date de paiement ne peut pas être dans le futur");
        }

        // Validate due date
        if (payment.getDatePaiement() == null) {
            errors.add("La date d'échéance est requise");
        }

        // Validate type
        if (payment.getDescription() == null) {
            errors.add("Le type de paiement est requis");
        }

        // // Validate status
        // if (payment.getPaymentDate() == null) {
        //     errors.add("Le statut du paiement est requis");
        // }

        // Validate description
        // if (payment.getDescription() != null && payment.getDescription().length() > 500) {
        //     errors.add("La description ne doit pas dépasser 500 caractères");
        // }

        // Validate receipt number if payment is PAID
        // if (payment.getType() == null || payment.getType().trim().isEmpty()) {
        //     errors.add("Le numéro de reçu est requis pour un paiement effectué");
        // }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
