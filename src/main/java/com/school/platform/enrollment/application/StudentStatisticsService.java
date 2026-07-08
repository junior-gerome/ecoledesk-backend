package com.school.platform.enrollment.application;

import com.school.platform.enrollment.domain.model.InscriptionStudent;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.InscriptionStudentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentStatisticsService {

    private final StudentRepository studentRepository;
    private final InscriptionStudentRepository inscriptionStudentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<InscriptionStudent> inscriptions = inscriptionStudentRepository.findAll();
        Map<Long, InscriptionStudent> latestEnrollmentByStudent = inscriptions.stream()
                .filter(inscription -> inscription.getStudent() != null && inscription.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        inscription -> inscription.getStudent().getId(),
                        inscription -> inscription,
                        (left, right) -> left.getId() != null && right.getId() != null && left.getId() > right.getId()
                                ? left
                                : right));

        long francophone = 0;
        long anglophone = 0;
        long newFrancophone = 0;
        long newAnglophone = 0;

        for (InscriptionStudent inscription : latestEnrollmentByStudent.values()) {
            String section = sectionLabel(inscription);
            boolean isNew = inscription.getDateInscription() != null
                    && !inscription.getDateInscription().isBefore(monthStart);
            if (section.contains("ANGLO")) {
                anglophone++;
                if (isNew) {
                    newAnglophone++;
                }
            } else {
                francophone++;
                if (isNew) {
                    newFrancophone++;
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("francophoneStudents", francophone);
        stats.put("anglophoneStudents", anglophone);
        stats.put("newFrancophoneStudents", newFrancophone);
        stats.put("newAnglophoneStudents", newAnglophone);
        stats.put("monthlyTrend", getMonthlyEnrollmentTrend(inscriptions));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Long> getMonthlyEnrollmentTrend(int year) {
        List<InscriptionStudent> inscriptions = inscriptionStudentRepository.findAll();
        List<Long> trend = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now().withYear(year);
        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            long count = inscriptions.stream()
                    .filter(inscription -> inscription.getDateInscription() != null)
                    .filter(inscription -> YearMonth.from(inscription.getDateInscription()).equals(month))
                    .map(InscriptionStudent::getStudent)
                    .filter(student -> student != null && student.getId() != null)
                    .map(Student::getId)
                    .distinct()
                    .count();
            trend.add(count);
        }
        return trend;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatsByClasseRoom(Long classeRoomId) {
        long totalStudents = inscriptionStudentRepository.countByClasseRoomId(classeRoomId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("classeRoomId", classeRoomId);
        stats.put("totalStudents", totalStudents);
        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAnnualStats(Long anneeId) {
        long totalStudents = inscriptionStudentRepository.countByAnneeScolaireId(anneeId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("anneeId", anneeId);
        stats.put("totalStudents", totalStudents);
        return stats;
    }

    private List<Long> getMonthlyEnrollmentTrend(List<InscriptionStudent> inscriptions) {
        List<Long> trend = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            long count = inscriptions.stream()
                    .filter(inscription -> inscription.getDateInscription() != null)
                    .filter(inscription -> YearMonth.from(inscription.getDateInscription()).equals(month))
                    .map(InscriptionStudent::getStudent)
                    .filter(student -> student != null && student.getId() != null)
                    .map(Student::getId)
                    .distinct()
                    .count();
            trend.add(count);
        }
        return trend;
    }

    private String sectionLabel(InscriptionStudent inscription) {
        if (inscription.getClasseRoom() == null || inscription.getClasseRoom().getSection() == null) {
            return "";
        }
        return String.valueOf(inscription.getClasseRoom().getSection().getLibelle()).toUpperCase();
    }
}
