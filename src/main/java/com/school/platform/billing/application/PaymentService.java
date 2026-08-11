package com.school.platform.billing.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.shared.domain.exception.BadRequestException;
import com.school.platform.shared.domain.exception.BusinessException;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.billing.application.dto.PaymentDTO;
import com.school.platform.billing.application.dto.PaymentRequest;
import com.school.platform.billing.application.dto.PaymentSummaryReport;
import com.school.platform.billing.domain.model.TypePaiement;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.billing.domain.model.Montant;
import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.billing.infrastructure.persistence.MontantRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final int RECEIPT_GENERATION_ATTEMPTS = 5;

    private final PaiementRepository paiementRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MontantRepository montantRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listPayments(
            Long studentId,
            String status,
            TypePaiement type,
            LocalDate startDate,
            LocalDate endDate) {
        return paiementRepository.search(studentId, type, startDate, endDate).stream()
                .map(this::toPaymentResponse)
                .filter(row -> status == null
                        || status.isBlank()
                        || status.equalsIgnoreCase(String.valueOf(row.get("status"))))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> summarizePayments() {
        BigDecimal paidAmount = BigDecimal.ZERO;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        BigDecimal lateAmount = BigDecimal.ZERO;

        for (Paiement payment : paiementRepository.search(null, null, null, null)) {
            String status = paymentStatus(payment);
            if ("CANCELLED".equals(status)) {
                continue;
            }

            BigDecimal paid = toBigDecimal(payment.getMontantPaye());
            BigDecimal remaining = toBigDecimal(payment.getMontantRestant());

            if ("PAID".equals(status)) {
                paidAmount = paidAmount.add(paid);
            } else if ("LATE".equals(status)) {
                lateAmount = lateAmount.add(remaining);
            } else {
                pendingAmount = pendingAmount.add(remaining);
            }
        }

        BigDecimal total = paidAmount.add(pendingAmount).add(lateAmount);
        return Map.of(
                "totalAmount", total,
                "paidAmount", paidAmount,
                "pendingAmount", pendingAmount,
                "lateAmount", lateAmount);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentResponse(Long id) {
        return toPaymentResponse(findPayment(id));
    }

    @Transactional
    public Map<String, Object> createPayment(PaymentRequest request) {
        Paiement payment = new Paiement();
        applyRequest(payment, request, true);
        Paiement saved = paiementRepository.save(payment);
        return toPaymentResponse(saved);
    }

    @Transactional
    public Map<String, Object> updatePayment(Long id, PaymentRequest request) {
        Paiement payment = findPaymentForUpdate(id);
        ensureNotCancelled(payment);
        applyRequest(payment, request, false);
        return toPaymentResponse(paiementRepository.save(payment));
    }

    @Transactional
    public void cancelPayment(Long id, String reason) {
        Paiement payment = findPaymentForUpdate(id);
        if (payment.isCancelled()) {
            return;
        }
        payment.setCancelledAt(LocalDateTime.now());
        payment.setCancellationReason(hasText(reason) ? reason.trim() : "Annulation administrative");
        paiementRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentReceiptDto(Long id) {
        return convertToDTO(findPayment(id));
    }

    @Transactional(readOnly = true)
    public PaymentSummaryReport generatePaymentSummaryReport(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            return generatePaymentSummaryReport(start, end);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Format de date invalide. Utilisez yyyy-MM-dd.");
        }
    }

    @Transactional(readOnly = true)
    public PaymentSummaryReport generatePaymentSummaryReport(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de debut et de fin sont obligatoires.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("La date de fin doit etre superieure ou egale a la date de debut.");
        }

        List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDate, endDate).stream()
                .filter(payment -> !payment.isCancelled())
                .toList();
        List<PaymentDTO> paymentDTOs = paiements.stream()
                .map(this::convertToDTO)
                .sorted(Comparator.comparing(PaymentDTO::getDatePaiement, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());

        Map<String, BigDecimal> amountByType = paiements.stream()
                .collect(Collectors.groupingBy(
                        paiement -> paiement.getTypePaiement() != null ? paiement.getTypePaiement().name() : "INCONNU",
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                paiement -> toBigDecimal(paiement.getMontantPaye()),
                                BigDecimal::add)));

        Map<String, BigDecimal> amountByMonth = paiements.stream()
                .collect(Collectors.groupingBy(
                        paiement -> paiement.getDatePaiement() != null
                                ? YearMonth.from(paiement.getDatePaiement()).toString()
                                : "INCONNU",
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                paiement -> toBigDecimal(paiement.getMontantPaye()),
                                BigDecimal::add)));

        Map<String, BigDecimal> amountByClass = paiements.stream()
                .collect(Collectors.groupingBy(
                        paiement -> paiement.getEnrollment() != null
                                && paiement.getEnrollment().getClassroom() != null
                                && paiement.getEnrollment().getClassroom().getNameClasse() != null
                                        ? paiement.getEnrollment().getClassroom().getNameClasse()
                                        : "INCONNU",
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                paiement -> toBigDecimal(paiement.getMontantPaye()),
                                BigDecimal::add)));

        Map<String, BigDecimal> unpaidByClass = paiements.stream()
                .collect(Collectors.groupingBy(
                        paiement -> paiement.getEnrollment() != null
                                && paiement.getEnrollment().getClassroom() != null
                                && paiement.getEnrollment().getClassroom().getNameClasse() != null
                                        ? paiement.getEnrollment().getClassroom().getNameClasse()
                                        : "INCONNU",
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                paiement -> toBigDecimal(paiement.getMontantRestant()),
                                BigDecimal::add)));

        Map<String, Integer> countByPaymentMethod = paiements.stream()
                .collect(Collectors.groupingBy(
                        paiement -> paiement.getPaymentMethod() != null ? paiement.getPaymentMethod() : "INCONNU",
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        PaymentSummaryReport report = new PaymentSummaryReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalAmount(paiements.stream()
                .map(p -> toBigDecimal(p.getMontantPaye()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        report.setAmountByType(amountByType);
        report.setAmountByMonth(amountByMonth);
        report.setAmountByClass(amountByClass);
        report.setUnpaidFees(unpaidByClass);
        report.setCountByPaymentMethod(countByPaymentMethod);
        report.setTotalPayments(paiements.size());
        report.setRecentPayments(paymentDTOs.stream().limit(10).collect(Collectors.toList()));

        return report;
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getPaymentsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return paiementRepository.findByDatePaiementBetween(startDate, endDate).stream()
                .filter(payment -> !payment.isCancelled())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void applyRequest(Paiement payment, PaymentRequest request, boolean creation) {
        if (request == null) {
            throw new BadRequestException("Le corps de la requete est requis.");
        }

        Long studentId = request.getStudentId() != null
                ? request.getStudentId()
                : payment.getStudent() == null ? null : payment.getStudent().getId();
        if (studentId == null) {
            throw new BadRequestException("L'identifiant de l'eleve est requis.");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Eleve", "id", studentId));
        Enrollment enrollment = enrollmentRepository
                .findTopByStudentIdAndStatusOrderByEnrollmentDateDesc(studentId, EnrollmentStatus.CONFIRMED)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", "studentId", studentId));
        if (enrollment.getClassroom() == null || enrollment.getClassroom().getId() == null) {
            throw new BadRequestException("L'eleve n'est rattache a aucune classe.");
        }

        TypePaiement type = request.getType() != null
                ? request.getType()
                : payment.getTypePaiement() == null ? TypePaiement.FRAIS_AUTRES : payment.getTypePaiement();
        Montant montant = montantRepository.findByClasseRoomIdAndTypePaiement(enrollment.getClassroom().getId(), type)
                .orElseGet(() -> montantRepository.findByClasseRoomId(enrollment.getClassroom().getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Montant", "classeRoomId", enrollment.getClassroom().getId())));

        BigDecimal amount = request.getAmount() != null ? request.getAmount() : payment.getMontantPaye();
        if (amount == null) {
            throw new BadRequestException("Le montant paye est requis.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le montant paye doit etre positif.");
        }

        BigDecimal discount = firstNonNull(request.getDiscount(), request.getRemise(), payment.getRemise(), BigDecimal.ZERO);
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("La remise ne peut pas etre negative.");
        }

        BigDecimal expectedAmount = toBigDecimal(montant.getCount());
        if (discount.compareTo(expectedAmount) > 0) {
            throw new BadRequestException("La remise ne peut pas depasser le montant attendu.");
        }

        BigDecimal payableAmount = expectedAmount.subtract(discount).max(BigDecimal.ZERO);
        if (amount.compareTo(payableAmount) > 0) {
            throw new BadRequestException("Le montant paye ne peut pas depasser le reste a payer.");
        }

        String requestedStatus = request.getStatus() == null ? null : request.getStatus().trim();
        if ("PAID".equalsIgnoreCase(requestedStatus) && amount.compareTo(payableAmount) < 0) {
            throw new BadRequestException("Un paiement marque comme solde doit couvrir tout le montant restant.");
        }

        LocalDate paymentDate = request.getPaymentDate() != null
                ? request.getPaymentDate()
                : payment.getDatePaiement() == null ? LocalDate.now() : payment.getDatePaiement();
        LocalDate dueDate = request.getDueDate() != null
                ? request.getDueDate()
                : payment.getDueDate() == null ? paymentDate : payment.getDueDate();

        payment.setStudent(student);
        payment.setEnrollment(enrollment);
        payment.setMontant(montant);
        payment.setTypePaiement(type);
        payment.setMontantPaye(amount);
        payment.setRemise(discount);
        payment.setMontantRestant(payableAmount.subtract(amount).max(BigDecimal.ZERO));
        payment.setDatePaiement(paymentDate);
        payment.setDueDate(dueDate);
        payment.setDescription(trimToNull(request.getDescription()));
        payment.setPaymentMethod(resolvePaymentMethod(request.getPaymentMethod(), payment.getPaymentMethod()));

        String receiptNumber = trimToNull(request.getReceiptNumber());
        if (receiptNumber != null) {
            boolean receiptExists = payment.getId() == null
                    ? paiementRepository.existsByReceiptNumber(receiptNumber)
                    : paiementRepository.existsByReceiptNumberAndIdNot(receiptNumber, payment.getId());
            if (receiptExists) {
                throw new BusinessException("Ce numero de recu existe deja.");
            }
            payment.setReceiptNumber(receiptNumber);
        } else if (creation && !hasText(payment.getReceiptNumber())) {
            payment.setReceiptNumber(generateReceiptNumber());
        }
    }

    private Paiement findPayment(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id));
    }

    private Paiement findPaymentForUpdate(Long id) {
        return paiementRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "id", id));
    }

    private void ensureNotCancelled(Paiement payment) {
        if (payment.isCancelled()) {
            throw new BusinessException("Un paiement annule ne peut pas etre modifie.");
        }
    }

    private Map<String, Object> toPaymentResponse(Paiement payment) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", payment.getId());
        row.put("studentId", payment.getStudent() == null ? null : payment.getStudent().getId());
        row.put("studentName", payment.getStudent() == null ? "" : fullName(payment.getStudent()));
        row.put("amount", toBigDecimal(payment.getMontantPaye()));
        row.put("remainingAmount", toBigDecimal(payment.getMontantRestant()));
        row.put("discount", toBigDecimal(payment.getRemise()));
        row.put("paymentDate", payment.getDatePaiement() == null ? "" : payment.getDatePaiement().toString());
        row.put("dueDate", payment.getDueDate() == null ? "" : payment.getDueDate().toString());
        row.put("type", payment.getTypePaiement() == null ? TypePaiement.FRAIS_AUTRES.name() : payment.getTypePaiement().name());
        row.put("status", paymentStatus(payment));
        row.put("description", payment.getDescription());
        row.put("paymentMethod", resolvePaymentMethod(null, payment.getPaymentMethod()));
        row.put("receiptNumber", payment.getReceiptNumber());
        row.put("cancelledAt", payment.getCancelledAt());
        row.put("cancellationReason", payment.getCancellationReason());
        return row;
    }

    private PaymentDTO convertToDTO(Paiement paiement) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(paiement.getId());
        dto.setStudent(paiement.getStudent());
        dto.setMontant(paiement.getMontant());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setMontantPaye(paiement.getMontantPaye());
        dto.setMontantRestant(paiement.getMontantRestant());
        dto.setRemise(paiement.getRemise());
        dto.setTypePaiement(paiement.getTypePaiement());
        dto.setDescription(paiement.getDescription());
        return dto;
    }

    private String paymentStatus(Paiement payment) {
        if (payment.isCancelled()) {
            return "CANCELLED";
        }
        if (payment.getMontantRestant() == null || payment.getMontantRestant().compareTo(BigDecimal.ZERO) == 0) {
            return "PAID";
        }
        if (payment.getDueDate() != null && payment.getDueDate().isBefore(LocalDate.now())) {
            return "LATE";
        }
        return "PENDING";
    }

    private String generateReceiptNumber() {
        for (int attempt = 0; attempt < RECEIPT_GENERATION_ATTEMPTS; attempt++) {
            String candidate = "PAY-" + LocalDate.now().toString().replace("-", "") + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (!paiementRepository.existsByReceiptNumber(candidate)) {
                return candidate;
            }
        }
        throw new BusinessException("Impossible de generer un numero de recu unique.");
    }

    private BigDecimal firstNonNull(BigDecimal... values) {
        for (BigDecimal value : values) {
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal toBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String resolvePaymentMethod(String requested, String current) {
        String value = trimToNull(requested);
        if (value == null) {
            value = trimToNull(current);
        }
        return value == null ? "CASH" : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String fullName(Student student) {
        return (safe(student.getLastNameStudent()) + " " + safe(student.getFirstNameStudent())).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}