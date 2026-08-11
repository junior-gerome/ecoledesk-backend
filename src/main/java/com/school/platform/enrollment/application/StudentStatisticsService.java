package com.school.platform.enrollment.application;

import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
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
    private final EnrollmentRepository enrollmentRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentStatistics() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        List<Enrollment> enrollments = enrollmentRepository.findByStatus(EnrollmentStatus.CONFIRMED);

        Map<Long, Enrollment> latestByStudent = enrollments.stream()
                .filter(e -> e.getStudent() != null && e.getStudent().getId() != null)
                .collect(Collectors.toMap(
                        e -> e.getStudent().getId(),
                        e -> e,
                        (left, right) -> left.getId() != null && right.getId() != null && left.getId() > right.getId()
                                ? left : right));

        long francophone = 0, anglophone = 0, newFrancophone = 0, newAnglophone = 0;

        for (Enrollment enrollment : latestByStudent.values()) {
            String section = sectionLabel(enrollment);
            boolean isNew = enrollment.getEnrollmentDate() != null
                    && !enrollment.getEnrollmentDate().isBefore(monthStart);
            if (section.contains("ANGLO")) {
                anglophone++;
                if (isNew) newAnglophone++;
            } else {
                francophone++;
                if (isNew) newFrancophone++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("francophoneStudents", francophone);
        stats.put("anglophoneStudents", anglophone);
        stats.put("newFrancophoneStudents", newFrancophone);
        stats.put("newAnglophoneStudents", newAnglophone);
        stats.put("monthlyTrend", getMonthlyEnrollmentTrend(enrollments));
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Long> getMonthlyEnrollmentTrend(int year) {
        List<Enrollment> enrollments = enrollmentRepository.findByStatus(EnrollmentStatus.CONFIRMED);
        return buildTrend(enrollments, YearMonth.now().withYear(year));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStatsByClasseRoom(Long classeRoomId) {
        long totalStudents = enrollmentRepository.countByClassroomIdAndStatus(classeRoomId, EnrollmentStatus.CONFIRMED);
        return Map.of("classeRoomId", classeRoomId, "totalStudents", totalStudents);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getYearStats(Long academicYearId) {
        long totalStudents = enrollmentRepository.countByAcademicYearIdAndStatus(academicYearId, EnrollmentStatus.CONFIRMED);
        return Map.of("academicYearId", academicYearId, "totalStudents", totalStudents);
    }

    private List<Long> getMonthlyEnrollmentTrend(List<Enrollment> enrollments) {
        return buildTrend(enrollments, YearMonth.now());
    }

    private List<Long> buildTrend(List<Enrollment> enrollments, YearMonth reference) {
        List<Long> trend = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            YearMonth month = reference.minusMonths(i);
            long count = enrollments.stream()
                    .filter(e -> e.getEnrollmentDate() != null)
                    .filter(e -> YearMonth.from(e.getEnrollmentDate()).equals(month))
                    .map(e -> e.getStudent().getId())
                    .distinct()
                    .count();
            trend.add(count);
        }
        return trend;
    }

    private String sectionLabel(Enrollment enrollment) {
        if (enrollment.getClassroom() == null || enrollment.getClassroom().getSection() == null) {
            return "";
        }
        return String.valueOf(enrollment.getClassroom().getSection().getLibelle()).toUpperCase();
    }
}
