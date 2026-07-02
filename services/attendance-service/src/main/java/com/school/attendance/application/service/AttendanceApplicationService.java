package com.school.attendance.application.service;

import com.school.attendance.application.port.in.AttendanceFilter;
import com.school.attendance.application.port.in.AttendanceSummaryRow;
import com.school.attendance.application.port.in.AttendanceUseCase;
import com.school.attendance.application.port.in.SaveDailyAttendanceCommand;
import com.school.attendance.application.port.out.AttendanceRepository;
import com.school.attendance.application.port.out.ClassRosterPort;
import com.school.attendance.application.port.out.ClassRosterStudent;
import com.school.attendance.domain.model.AttendanceRecord;
import com.school.attendance.domain.model.AttendanceStatus;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AttendanceApplicationService implements AttendanceUseCase {
    private static final Logger log = LoggerFactory.getLogger(AttendanceApplicationService.class);

    private final AttendanceRepository repository;
    private final ClassRosterPort classRosterPort;

    public AttendanceApplicationService(AttendanceRepository repository, ClassRosterPort classRosterPort) {
        this.repository = repository;
        this.classRosterPort = classRosterPort;
    }

    @Override
    public List<AttendanceRecord> list(AttendanceFilter filter) {
        if (filter.classId() != null && filter.date() != null) {
            return listDailyClassAttendance(filter);
        }
        return repository.findAll(filter);
    }

    @Override
    public List<AttendanceRecord> saveDailyAttendance(SaveDailyAttendanceCommand command) {
        if (command.entries() == null || command.entries().isEmpty()) {
            return List.of();
        }
        return command.entries().stream()
                .map(entry -> new AttendanceRecord(null, entry.studentId(), entry.studentName(),
                        command.classId(), command.className(), command.date(), entry.status(),
                        entry.hours(), false, null, null))
                .map(repository::save)
                .toList();
    }

    @Override
    public AttendanceRecord updateJustification(Long recordId, String note, boolean justified) {
        AttendanceRecord record = repository.findById(recordId)
                .orElseThrow(() -> new AttendanceNotFoundException(recordId));
        return repository.save(record.justify(note, justified));
    }

    @Override
    public void delete(Long recordId) {
        repository.findById(recordId).orElseThrow(() -> new AttendanceNotFoundException(recordId));
        repository.deleteById(recordId);
    }

    @Override
    public List<AttendanceSummaryRow> summarize(AttendanceFilter filter) {
        Map<Long, List<AttendanceRecord>> byStudent = repository.findAll(filter).stream()
                .collect(Collectors.groupingBy(
                        AttendanceRecord::studentId,
                        LinkedHashMap::new,
                        Collectors.toList()));

        return byStudent.values().stream()
                .filter(records -> records.stream()
                        .anyMatch(record -> record.status() != AttendanceStatus.PRESENT))
                .map(records -> {
                    AttendanceRecord latest = records.stream()
                            .max(Comparator.comparing(AttendanceRecord::date))
                            .orElseThrow();
                    long absences = records.stream().filter(record -> record.status() == AttendanceStatus.ABSENT).count();
                    long lates = records.stream().filter(record -> record.status() == AttendanceStatus.LATE).count();
                    long unjustified = records.stream()
                            .filter(record -> record.status() != AttendanceStatus.PRESENT)
                            .filter(record -> !record.justified())
                            .count();
                    return new AttendanceSummaryRow(latest.studentId(), latest.studentName(), latest.className(),
                            absences, lates, unjustified, latest.date());
                })
                .toList();
    }

    private List<AttendanceRecord> listDailyClassAttendance(AttendanceFilter filter) {
        List<ClassRosterStudent> roster;
        try {
            roster = classRosterPort.findStudentsByClass(filter.classId(), filter.schoolYearId());
        } catch (RuntimeException ex) {
            log.warn("Unable to load class roster from school API for class {}: {}", filter.classId(), ex.getMessage());
            return repository.findAll(filter);
        }

        if (roster.isEmpty()) {
            return repository.findAll(filter);
        }

        Map<Long, AttendanceRecord> savedByStudent = repository.findAll(filter).stream()
                .collect(Collectors.toMap(
                        AttendanceRecord::studentId,
                        Function.identity(),
                        (left, right) -> left,
                        LinkedHashMap::new));

        return roster.stream()
                .map(student -> mergeDailyRecord(student, filter, savedByStudent.get(student.studentId())))
                .toList();
    }

    private AttendanceRecord mergeDailyRecord(
            ClassRosterStudent student,
            AttendanceFilter filter,
            AttendanceRecord savedRecord
    ) {
        if (savedRecord != null) {
            return savedRecord;
        }

        return new AttendanceRecord(
                0L,
                student.studentId(),
                student.studentName(),
                filter.classId(),
                student.className(),
                filter.date(),
                AttendanceStatus.PRESENT,
                BigDecimal.ZERO,
                false,
                null,
                null);
    }
}
