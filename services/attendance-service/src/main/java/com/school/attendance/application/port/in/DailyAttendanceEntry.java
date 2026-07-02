package com.school.attendance.application.port.in;

import com.school.attendance.domain.model.AttendanceStatus;
import java.math.BigDecimal;

public record DailyAttendanceEntry(
        Long studentId,
        String studentName,
        AttendanceStatus status,
        BigDecimal hours
) {
}
