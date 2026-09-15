package com.school.platform.billing.application.dto;

import java.time.LocalDate;

import com.school.platform.billing.domain.model.TypePaiement;

/**
 * Filters accepted by the payment export endpoints.
 *
 * <p>The backend always re-validates these values server-side: neither the
 * academic year nor any other scope can be bypassed by the caller.</p>
 */
public record PaymentExportRequest(
        Long studentId,
        Long classroomId,
        Long academicYearId,
        String q,
        String receiptNumber,
        TypePaiement type,
        String status,
        String paymentMethod,
        LocalDate startDate,
        LocalDate endDate) {

    public static PaymentExportRequest empty() {
        return new PaymentExportRequest(null, null, null, null, null, null, null, null, null, null);
    }
}