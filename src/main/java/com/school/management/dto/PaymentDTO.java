package com.school.management.dto;

import java.time.LocalDate;

import com.school.management.model.*;

import lombok.Data;

@Data
public class PaymentDTO {

   private Long id;
    private Student student;
    private Montant montant;
    private LocalDate datePaiement;
    private Long montantPaye;
    private Long montantRestant;
    private Long remise;
    //private TypePaiement typePaiement;
    private String description;

    // La méthode ci-dessous est inutile avec Lombok, sauf si tu veux un alias spécifique
    // public String getPaymentType() {
    //     return type;
    // }
}
