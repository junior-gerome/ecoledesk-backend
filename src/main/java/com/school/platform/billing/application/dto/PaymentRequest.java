package com.school.platform.billing.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.school.platform.billing.domain.model.TypePaiement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequest {
    @Positive(message = "L'identifiant de l'eleve doit etre positif")
    private Long studentId;

    private TypePaiement type;

    @DecimalMin(value = "0.01", message = "Le montant paye doit etre positif")
    private BigDecimal amount;

    @DecimalMin(value = "0.00", message = "La remise ne peut pas etre negative")
    private BigDecimal discount;

    @DecimalMin(value = "0.00", message = "La remise ne peut pas etre negative")
    private BigDecimal remise;

    private LocalDate paymentDate;
    private LocalDate dueDate;

    @Size(max = 20, message = "Le statut ne doit pas depasser 20 caracteres")
    private String status;

    @Size(max = 500, message = "La description ne doit pas depasser 500 caracteres")
    private String description;

    @Size(max = 40, message = "La methode de paiement ne doit pas depasser 40 caracteres")
    private String paymentMethod;

    @Size(max = 80, message = "Le numero de recu ne doit pas depasser 80 caracteres")
    private String receiptNumber;
}