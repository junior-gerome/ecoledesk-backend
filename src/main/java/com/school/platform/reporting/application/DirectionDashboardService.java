package com.school.platform.reporting.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.reporting.application.dto.dashboard.DirectionDashboardDTO;
import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.billing.domain.model.Paiement;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.academic.infrastructure.persistence.ClasseRoomRepository;
import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.reporting.infrastructure.persistence.LogActiviteRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DirectionDashboardService {
    private static final BigDecimal WEAK_GRADE_LIMIT = BigDecimal.TEN;
    private static final BigDecimal EXCELLENT_GRADE_LIMIT = BigDecimal.valueOf(16);

    private final AbsenceRepository absenceRepository;
    private final PaiementRepository paiementRepository;
    private final GradeRepository gradeRepository;
    private final ClasseRoomRepository classeRoomRepository;
    private final InscriptionStudentRepository inscriptionStudentRepository;
    private final LogActiviteRepository logActiviteRepository;

    @Transactional(readOnly = true)
    public DirectionDashboardDTO getSummary() {
        LocalDate today = LocalDate.now();

        long overduePayments = 0;
        long overdueAmount = 0;
        for (Paiement paiement : paiementRepository.findAll()) {
            BigDecimal remaining = paiement.getMontantRestant();
            LocalDate dueDate = paiement.getDueDate();
            if (remaining != null && remaining.compareTo(BigDecimal.ZERO) > 0 && dueDate != null && dueDate.isBefore(today)) {
                overduePayments++;
                overdueAmount += remaining.longValue();
            }
        }

        long overloadedClasses = classeRoomRepository.findAll().stream()
                .filter(this::isOverloaded)
                .count();

        return new DirectionDashboardDTO(
                absenceRepository.countByDate(today),
                overduePayments,
                overdueAmount,
                gradeRepository.countByGradeGreaterThanEqual(EXCELLENT_GRADE_LIMIT),
                gradeRepository.countByGradeLessThan(WEAK_GRADE_LIMIT),
                overloadedClasses,
                inscriptionStudentRepository.countByStatutPreinscriptionIn(List.of("BROUILLON", "EN_ATTENTE")),
                logActiviteRepository.count());
    }

    private boolean isOverloaded(ClasseRoom classeRoom) {
        Integer capacity = classeRoom.getCapacity();
        if (capacity == null || capacity <= 0 || classeRoom.getId() == null) {
            return false;
        }

        return inscriptionStudentRepository.countByClasseRoomId(classeRoom.getId()) > capacity;
    }
}
