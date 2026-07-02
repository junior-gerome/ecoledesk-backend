package com.school.attendance.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceRecord(
        Long id,
        Long studentId,
        String studentName,
        Long classId,
        String className,
        LocalDate date,
        AttendanceStatus status,
        BigDecimal hours,
        boolean justified,
        String justificationNote,
        LocalDateTime updatedAt
) {
    public AttendanceRecord {
        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("A valid student id is required.");
        }
        if (studentName == null || studentName.isBlank()) {
            throw new IllegalArgumentException("Student name is required.");
        }
        if (classId == null || classId <= 0) {
            throw new IllegalArgumentException("A valid class id is required.");
        }
        if (className == null || className.isBlank()) {
            throw new IllegalArgumentException("Class name is required.");
        }
        if (date == null) {
            throw new IllegalArgumentException("Attendance date is required.");
        }
        if (status == null) {
            status = AttendanceStatus.PRESENT;
        }
        if (hours == null) {
            hours = BigDecimal.ZERO;
        }
        if (hours.signum() < 0) {
            throw new IllegalArgumentException("Attendance hours cannot be negative.");
        }
        if (status == AttendanceStatus.PRESENT) {
            justified = false;
            justificationNote = null;
        }
    }

    public AttendanceRecord justify(String note, boolean nextJustified) {
        if (status == AttendanceStatus.PRESENT) {
            throw new IllegalArgumentException("A present record cannot be justified.");
        }
        return new AttendanceRecord(id, studentId, studentName, classId, className, date, status,
                hours, nextJustified, note == null || note.isBlank() ? null : note.trim(), updatedAt);
    }
}
