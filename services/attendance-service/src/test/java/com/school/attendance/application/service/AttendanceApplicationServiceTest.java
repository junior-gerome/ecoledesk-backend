package com.school.attendance.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.school.attendance.application.port.in.AttendanceFilter;
import com.school.attendance.application.port.out.AttendanceRepository;
import com.school.attendance.application.port.out.ClassRosterPort;
import com.school.attendance.application.port.out.ClassRosterStudent;
import com.school.attendance.domain.model.AttendanceRecord;
import com.school.attendance.domain.model.AttendanceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AttendanceApplicationServiceTest {
    private final AttendanceRepository repository = Mockito.mock(AttendanceRepository.class);
    private final ClassRosterPort classRosterPort = Mockito.mock(ClassRosterPort.class);
    private final AttendanceApplicationService service = new AttendanceApplicationService(repository, classRosterPort);

    @Test
    void listReturnsFullClassRosterWhenClassAndDateAreProvided() {
        LocalDate date = LocalDate.of(2026, 5, 21);
        AttendanceFilter filter = new AttendanceFilter(1L, date, null, null, null, null, null);

        when(classRosterPort.findStudentsByClass(1L, null)).thenReturn(List.of(
                new ClassRosterStudent(10L, "Ada Lovelace", 1L, "CM2"),
                new ClassRosterStudent(11L, "Bob Martin", 1L, "CM2")
        ));
        when(repository.findAll(filter)).thenReturn(List.of(
                record(10L, AttendanceStatus.ABSENT, false, date, 4L)
        ));

        var result = service.list(filter);

        assertThat(result).hasSize(2);
        assertThat(result.stream().filter(row -> row.studentId().equals(10L)).findFirst())
                .get()
                .satisfies(row -> {
                    assertThat(row.status()).isEqualTo(AttendanceStatus.ABSENT);
                    assertThat(row.id()).isEqualTo(4L);
                });
        assertThat(result.stream().filter(row -> row.studentId().equals(11L)).findFirst())
                .get()
                .satisfies(row -> {
                    assertThat(row.status()).isEqualTo(AttendanceStatus.PRESENT);
                    assertThat(row.id()).isZero();
                });
    }

    @Test
    void summarizeIgnoresStudentsWithOnlyPresentRecords() {
        AttendanceFilter filter = new AttendanceFilter(1L, null, null, null, null, null);
        when(repository.findAll(filter)).thenReturn(List.of(
                record(1L, AttendanceStatus.PRESENT, false, LocalDate.of(2026, 5, 1), 1L),
                record(2L, AttendanceStatus.ABSENT, false, LocalDate.of(2026, 5, 1), 2L)
        ));

        var summary = service.summarize(filter);

        assertThat(summary).hasSize(1);
        assertThat(summary.getFirst().studentId()).isEqualTo(2L);
    }

    @Test
    void summarizeCountsAbsencesLatesAndUnjustifiedRecords() {
        AttendanceFilter filter = new AttendanceFilter(1L, null, null, null, null, null);
        when(repository.findAll(filter)).thenReturn(List.of(
                record(1L, AttendanceStatus.ABSENT, false, LocalDate.of(2026, 5, 1), 1L),
                record(1L, AttendanceStatus.LATE, true, LocalDate.of(2026, 5, 2), 2L),
                record(1L, AttendanceStatus.PRESENT, false, LocalDate.of(2026, 5, 3), 3L)
        ));

        var summary = service.summarize(filter);

        assertThat(summary).hasSize(1);
        assertThat(summary.getFirst().totalAbsences()).isEqualTo(1);
        assertThat(summary.getFirst().totalLates()).isEqualTo(1);
        assertThat(summary.getFirst().unjustifiedCount()).isEqualTo(1);
        assertThat(summary.getFirst().lastDate()).isEqualTo(LocalDate.of(2026, 5, 3));
    }

    private AttendanceRecord record(
            Long studentId,
            AttendanceStatus status,
            boolean justified,
            LocalDate date,
            Long id
    ) {
        return new AttendanceRecord(id, studentId, "Ada", 1L, "CM2", date, status,
                BigDecimal.ONE, justified, null, null);
    }
}
