package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.enrollment.domain.model.Student;

import lombok.Data;

@Data
public class PaymentDTO {

   private Long id;
    private Student student;
    private Montant montant;
    private LocalDate datePaiement;
    private BigDecimal montantPaye;
    private BigDecimal montantRestant;
    private BigDecimal remise;
    private TypePaiement typePaiement;
    private String description;

    // La méthode ci-dessous est inutile avec Lombok, sauf si tu veux un alias spécifique
    // public String getPaymentType() {
    //     return type;
    // }
}
