package com.school.management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.school.management.model.Student;

import lombok.Data;

@Data
public class PaymentDTO {

   private Long id;
    private Student student;
    private BigDecimal montant;
    private LocalDate datePaiement;
    //private TypePaiement typePaiement;
    private String description;

    // La méthode ci-dessous est inutile avec Lombok, sauf si tu veux un alias spécifique
    // public String getPaymentType() {
    //     return type;
    // }
}
