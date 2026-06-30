package com.school.attendance.adapter.in.rest;

import com.school.attendance.domain.model.AttendanceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
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
}
