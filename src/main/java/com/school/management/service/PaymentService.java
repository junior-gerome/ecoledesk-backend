package com.school.management.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.school.management.dto.GradeDTO;
import com.school.management.dto.PaymentDTO;
import com.school.management.dto.PaymentSummaryReport;
import com.school.management.dto.StudentDTO;
import com.school.management.model.Paiement;
import com.school.management.repository.PaiementRepository;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaiementRepository paiementRepository;

    // public List<PaymentDTO> getStudentPayments(Long studentId) {
    //     return paiementRepository.findByStudentId(studentId).stream()
    //             .map(this::convertToDTO)
    //             .toList();
    // }

    public PaymentSummaryReport generatePaymentSummaryReport(String startDate, String endDate) {
        // TODO: Implement the logic to generate payment summary report
        return new PaymentSummaryReport();
    }

    public List<PaymentDTO> getPaymentsBetweenDates(LocalDate startDate, LocalDate endDate) {
        return paiementRepository.findByDatePaiementBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .toList();
    }

    // public List<StudentDTO> findByClassId(String classId) {
    //     // Implémentation à venir
    //     return null;
    // }

    public List<GradeDTO> findByClassIdAndPeriod(String classId, String period) {
        // Implémentation à venir
        return null;
    }

    // public List<PaymentDTO> findByDateRange(LocalDate startDate, LocalDate endDate) {
    //     List<Paiement> paiements = paiementRepository.findByDatePaiementBetween(startDate, endDate);
    //     return paiements.stream()
    //             .map(this::convertToPaymentDTO)
    //             .collect(Collectors.toList());
    // }

    // private PaymentDTO convertToPaymentDTO(Paiement paiement) {
    //     PaymentDTO dto = new PaymentDTO();
    //     dto.setId(paiement.getId());
    //     dto.setMontant(paiement.getMontant().getId());
    //     dto.setDatePaiement(paiement.getDatePaiement());
    //    // dto.setTypePaiement();
    //     dto.setDescription(paiement.getDescription());
        
    //     // if (paiement.getStudent() != null) {
    //     //     dto.setStudentId(paiement.getStudent().getId());
    //     //     dto.setStudentName(paiement.getStudent().getNom() + " " + paiement.getStudent().getPrenom());
    //     //     if (paiement.getStudent().getClasse() != null) {
    //     //         dto.setClassName(paiement.getStudent().getClasse().getLibelle());
    //     //     }
    //     // }
        
    //     // // Génération du numéro de reçu
    //     // dto.setReceiptNumber(String.format("REC-%d-%s", 
    //     //     paiement.getId(), 
    //     //     paiement.getDatePaiement().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
    //     // ));
        
    //     return dto;
    // }

    private PaymentDTO convertToDTO(Object paiement) {
        return new PaymentDTO(); // TODO: Implement proper conversion
    }
}
