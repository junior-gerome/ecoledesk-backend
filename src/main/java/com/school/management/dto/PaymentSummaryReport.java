package com.school.management.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class PaymentSummaryReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Map<String, BigDecimal> amountByType;
    private Map<String, BigDecimal> amountByMonth;
    private Map<String, BigDecimal> unpaidFees;
    private int totalPayments;
    private List<PaymentDTO> recentPayments;
    private Map<String, BigDecimal> amountByClass;
    private String classId;
    private Map<String, Integer> countByPaymentMethod;
}