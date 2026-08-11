package com.school.platform.attendance.application;

import com.school.platform.attendance.application.dto.AttendanceRecordDTO;
import com.school.platform.attendance.application.dto.AttendanceSummaryDTO;
import com.school.platform.attendance.application.dto.DailyAttendanceRequest;
import com.school.platform.attendance.application.dto.DailyAttendanceEntryDTO;
import com.school.platform.attendance.application.dto.JustificationRequest;
import com.school.platform.attendance.application.mapper.AttendanceMapper;
import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.attendance.infrastructure.persistence.AbsenceRepository;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.enrollment.infrastructure.persistence.StudentRepository;
import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final EnrollmentRepository enrollmentRepository;
    private final AbsenceRepository absenceRepository;
    private final StudentRepository studentRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getRecords(
            Long classId, LocalDate date, LocalDate dateFrom, LocalDate dateTo,
            String statuses, Boolean justified, Long academicYearId) {

        if (classId != null && date != null) {
            return getRosterRecordsForDate(classId, date, academicYearId);
        }

        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();
        List<String> statusFilter = parseStatusFilter(statuses);

        List<Absence> absences = classId != null
                ? getAbsencesForClass(classId, academicYearId, start, end)
                : absenceRepository.findByDateBetween(start, end);

        return absences.stream()
                .filter(a -> statusFilter.isEmpty() || statusFilter.contains(a.getStatus()))
                .filter(a -> justified == null || Boolean.TRUE.equals(a.getJustified()) == justified)
                .map(a -> attendanceMapper.toRecordDTO(a, findEnrollment(a.getStudent().getId()), a.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<AttendanceRecordDTO> saveDaily(DailyAttendanceRequest request) {
        if (request == null || request.getClassId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'identifiant de la classe est requis.");
        }
        if (request.getDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de pointage est requise.");
        }
        if (request.getEntries() == null || request.getEntries().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La liste des pointages est requise.");
        }

        return request.getEntries().stream()
                .filter(entry -> entry.getStudentId() != null)
                .map(entry -> persistEntry(entry, request.getDate()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceRecordDTO updateJustification(Long id, JustificationRequest request) {
        Absence absence = absenceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pointage introuvable"));
        absence.setJustified(Boolean.TRUE.equals(request.getJustified()));
        absence.setJustificationNote(request.getJustificationNote());
        Absence saved = absenceRepository.save(absence);
        return attendanceMapper.toRecordDTO(saved, findEnrollment(saved.getStudent().getId()), saved.getDate());
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        absenceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceSummaryDTO> getSummary(Long classId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate start = dateFrom != null ? dateFrom : LocalDate.now().minusDays(30);
        LocalDate end = dateTo != null ? dateTo : LocalDate.now();

        List<Absence> absences = classId != null
                ? getAbsencesForClass(classId, null, start, end)
                : absenceRepository.findByDateBetween(start, end);

        Map<Long, List<Absence>> byStudent = absences.stream()
                .filter(a -> !"PRESENT".equals(a.getStatus()))
                .collect(Collectors.groupingBy(a -> a.getStudent().getId()));

        return byStudent.entrySet().stream()
                .map(entry -> {
                    Student student = entry.getValue().get(0).getStudent();
                    Enrollment enrollment = findEnrollment(student.getId());
                    return attendanceMapper.toSummaryDTO(student, enrollment, entry.getValue(), start);
                })
                .collect(Collectors.toList());
    }

    // --- private helpers ---

    private List<AttendanceRecordDTO> getRosterRecordsForDate(Long classId, LocalDate date, Long academicYearId) {
        List<Enrollment> roster = findRoster(classId, academicYearId);
        if (roster.isEmpty()) return List.of();

        List<Long> studentIds = roster.stream()
                .map(e -> e.getStudent().getId())
                .collect(Collectors.toList());

        Map<Long, Absence> recordByStudent = absenceRepository
                .findByStudentIdInAndDate(studentIds, date)
                .stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a, (a, b) -> a));

        return roster.stream()
                .map(e -> attendanceMapper.toRecordDTO(
                        recordByStudent.get(e.getStudent().getId()), e.getStudent(), e, date))
                .collect(Collectors.toList());
    }

    private List<Absence> getAbsencesForClass(Long classId, Long academicYearId, LocalDate start, LocalDate end) {
        List<Long> studentIds = findRoster(classId, academicYearId).stream()
                .map(e -> e.getStudent().getId())
                .collect(Collectors.toList());
        return studentIds.isEmpty()
                ? List.of()
                : absenceRepository.findByStudentIdInAndDateBetween(studentIds, start, end);
    }

    private AttendanceRecordDTO persistEntry(DailyAttendanceEntryDTO entry, LocalDate date) {
        Student student = studentRepository.findById(entry.getStudentId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Eleve introuvable: " + entry.getStudentId()));

        Absence absence = absenceRepository
                .findByStudentIdAndDate(entry.getStudentId(), date)
                .orElseGet(Absence::new);

        absence.setStudent(student);
        absence.setDate(date);
        absence.setStatus(validStatus(entry.getStatus()));
        absence.setHours(validHours(entry.getHours()));
        if (absence.getJustified() == null) {
            absence.setJustified(false);
        }

        Absence saved = absenceRepository.save(absence);
        return attendanceMapper.toRecordDTO(saved, student, findEnrollment(student.getId()), date);
    }

    private Enrollment findEnrollment(Long studentId) {
        return enrollmentRepository
                .findTopByStudentIdAndStatusOrderByEnrollmentDateDesc(studentId, EnrollmentStatus.CONFIRMED)
                .orElse(null);
    }

    private List<Enrollment> findRoster(Long classId, Long academicYearId) {
        List<Enrollment> roster = enrollmentRepository
                .findRosterByClassroom(classId, EnrollmentStatus.CONFIRMED, academicYearId);
        return roster.isEmpty() && academicYearId != null
                ? enrollmentRepository.findRosterByClassroom(classId, EnrollmentStatus.CONFIRMED, null)
                : roster;
    }

    private List<String> parseStatusFilter(String statuses) {
        if (statuses == null || statuses.isBlank()) return List.of();
        return Arrays.stream(statuses.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private String validStatus(String value) {
        String status = value == null ? "PRESENT" : value.trim().toUpperCase();
        if (!List.of("PRESENT", "ABSENT", "LATE", "EXCUSED").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut de presence invalide: " + value);
        }
        return status;
    }

    private Integer validHours(Integer hours) {
        int value = hours == null ? 0 : hours;
        if (value < 0 || value > 24) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nombre d'heures doit etre compris entre 0 et 24.");
        }
        return value;
    }
}
