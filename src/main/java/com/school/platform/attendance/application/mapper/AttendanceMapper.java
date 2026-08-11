package com.school.platform.attendance.application.mapper;

import com.school.platform.attendance.application.dto.AttendanceRecordDTO;
import com.school.platform.attendance.application.dto.AttendanceSummaryDTO;
import com.school.platform.attendance.domain.model.Absence;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.model.Student;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {

    public AttendanceRecordDTO toRecordDTO(Absence absence, Student student, Enrollment enrollment, LocalDate date) {
        Long classId = null;
        String className = "Non affecte";
        if (enrollment != null && enrollment.getClassroom() != null) {
            classId = enrollment.getClassroom().getId();
            className = enrollment.getClassroom().getNameClasse();
        }
        return AttendanceRecordDTO.builder()
                .id(absence == null ? 0L : absence.getId())
                .studentId(student.getId())
                .studentName(student.getFirstNameStudent() + " " + student.getLastNameStudent())
                .firstName(student.getFirstNameStudent())
                .lastName(student.getLastNameStudent())
                .classId(classId)
                .className(className)
                .date(date)
                .status(absence == null ? "PRESENT" : absence.getStatus())
                .hours(absence == null || absence.getHours() == null ? 0 : absence.getHours())
                .justified(absence != null && Boolean.TRUE.equals(absence.getJustified()))
                .justificationNote(absence == null ? null : absence.getJustificationNote())
                .updatedAt(absence == null || absence.getUpdatedAt() == null
                        ? LocalDateTime.now().toString()
                        : absence.getUpdatedAt().toString())
                .build();
    }

    public AttendanceRecordDTO toRecordDTO(Absence absence, Enrollment enrollment, LocalDate date) {
        return toRecordDTO(absence, enrollment.getStudent(), enrollment, date);
    }

    public AttendanceSummaryDTO toSummaryDTO(Student student, Enrollment enrollment, List<Absence> absences, LocalDate fallbackDate) {
        String className = enrollment == null || enrollment.getClassroom() == null
                ? "Non affecte"
                : enrollment.getClassroom().getNameClasse();
        long totalAbsences = absences.stream().filter(a -> "ABSENT".equals(a.getStatus())).count();
        long totalLates = absences.stream().filter(a -> "LATE".equals(a.getStatus())).count();
        long unjustified = absences.stream().filter(a -> !Boolean.TRUE.equals(a.getJustified())).count();
        LocalDate lastDate = absences.stream()
                .map(Absence::getDate)
                .max(LocalDate::compareTo)
                .orElse(fallbackDate);
        return AttendanceSummaryDTO.builder()
                .studentId(student.getId())
                .studentName(student.getFirstNameStudent() + " " + student.getLastNameStudent())
                .className(className)
                .totalAbsences(totalAbsences)
                .totalLates(totalLates)
                .unjustifiedCount(unjustified)
                .lastDate(lastDate)
                .build();
    }
}
