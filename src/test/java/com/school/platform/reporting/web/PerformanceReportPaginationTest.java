package com.school.platform.reporting.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.school.platform.academic.infrastructure.persistence.GradeRepository;
import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.billing.infrastructure.persistence.PaiementRepository;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;

class PerformanceReportPaginationTest {

    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final GradeRepository gradeRepository = mock(GradeRepository.class);
    private final AbsenceRepository absenceRepository = mock(AbsenceRepository.class);
    private final PaiementRepository paiementRepository = mock(PaiementRepository.class);

    private final ReportController controller = new ReportController(
            null, enrollmentRepository, gradeRepository, absenceRepository, paiementRepository);

    @BeforeEach
    void setUp() {
        List<Enrollment> enrollments = enrollments();
        List<Object[]> averages = averages();
        List<Absence> absences = absences();
        when(enrollmentRepository.findByStatusFetchStudentAndClassroom(EnrollmentStatus.CONFIRMED))
                .thenReturn(enrollments);
        when(gradeRepository.findAverageGradeByStudentIds(anyList()))
                .thenReturn(averages);
        when(absenceRepository.findByStudentIdInAndDateBetween(anyList(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(absences);
    }

    @Test
    void paginatesRiskStudentsServerSideAndReportsTotalElements() {
        Map<String, Object> page0 = body(controller.getPerformanceReport(null, null, null, 0, 2));
        Map<String, Object> page1 = body(controller.getPerformanceReport(null, null, null, 1, 2));

        assertThat(page0.get("riskTotalElements")).isEqualTo(3L);
        assertThat(page0.get("riskStudents")).isInstanceOf(List.class);
        List<?> riskPage0 = (List<?>) page0.get("riskStudents");
        List<?> riskPage1 = (List<?>) page1.get("riskStudents");

        assertThat(riskPage0).hasSize(2);
        assertThat(riskPage1).hasSize(1);
        assertThat(studentId(riskPage0.get(0))).isEqualTo(4L);
        assertThat(studentId(riskPage1.get(0))).isEqualTo(5L);
    }

    @Test
    void summaryAndRankingAreComputedInASinglePass() {
        Map<String, Object> body = body(controller.getPerformanceReport(null, null, null, 0, 10));

        assertThat(body.get("riskTotalElements")).isEqualTo(3L);
        @SuppressWarnings("unchecked")
        Map<String, Object> summary = (Map<String, Object>) body.get("summary");
        assertThat(summary.get("totalStudents")).isEqualTo(5);
        assertThat(summary.get("atRiskCount")).isEqualTo(3L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> topStudents = (List<Map<String, Object>>) body.get("topStudents");
        assertThat(topStudents).hasSize(5);
        assertThat(topStudents.get(0).get("studentId")).isEqualTo(1L);
        assertThat(topStudents.get(0).get("rank")).isEqualTo(1);
        assertThat(topStudents.get(4).get("rank")).isEqualTo(5);
    }

    @Test
    void clampsNegativePageAndExcessiveSize() {
        Map<String, Object> body = body(controller.getPerformanceReport(null, null, null, -3, 999));

        @SuppressWarnings("unchecked")
        List<?> riskStudents = (List<?>) body.get("riskStudents");
        assertThat(riskStudents).hasSize(3);
        assertThat(body.get("riskTotalElements")).isEqualTo(3L);
    }

    private Map<String, Object> body(ResponseEntity<Map<String, Object>> response) {
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private Object studentId(Object row) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) row;
        return map.get("studentId");
    }

    private List<Enrollment> enrollments() {
        List<Enrollment> enrollments = new ArrayList<>();
        for (long id = 1; id <= 5; id++) {
            Student student = mock(Student.class);
            when(student.getId()).thenReturn(id);
            when(student.getFirstNameStudent()).thenReturn("Prenom" + id);
            when(student.getLastNameStudent()).thenReturn("Nom" + id);
            Enrollment enrollment = mock(Enrollment.class);
            when(enrollment.getStudent()).thenReturn(student);
            when(enrollment.getClassroom()).thenReturn(null);
            enrollments.add(enrollment);
        }
        return enrollments;
    }

    private List<Object[]> averages() {
        return List.of(
                new Object[] { 1L, 16.0 },
                new Object[] { 2L, 13.0 },
                new Object[] { 3L, 9.0 },
                new Object[] { 4L, 12.0 },
                new Object[] { 5L, 8.0 });
    }

    private List<Absence> absences() {
        List<Absence> absences = new ArrayList<>();
        Student absentStudent = mock(Student.class);
        when(absentStudent.getId()).thenReturn(4L);
        for (int i = 0; i < 5; i++) {
            Absence absence = mock(Absence.class);
            when(absence.getStatus()).thenReturn("ABSENT");
            when(absence.getStudent()).thenReturn(absentStudent);
            absences.add(absence);
        }
        Absence present = mock(Absence.class);
        when(present.getStatus()).thenReturn("PRESENT");
        when(present.getStudent()).thenReturn(absentStudent);
        absences.add(present);
        return absences;
    }

    @Test
    void usesBulkQueriesInsteadOfPerStudentNPlusOne() {
        controller.getPerformanceReport(null, null, null, 0, 10);

        org.mockito.Mockito.verify(gradeRepository).findAverageGradeByStudentIds(anyList());
        org.mockito.Mockito.verify(absenceRepository)
                .findByStudentIdInAndDateBetween(anyList(), any(LocalDate.class), any(LocalDate.class));
        org.mockito.Mockito.verify(enrollmentRepository)
                .findByStatusFetchStudentAndClassroom(eq(EnrollmentStatus.CONFIRMED));
    }
}
