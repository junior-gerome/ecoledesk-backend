package com.school.attendance.application.port.in;

import java.time.LocalDate;

public record AttendanceSummaryRow(
        Long studentId,
        String studentName,
        String className,
        long totalAbsences,
        long totalLates,
        long unjustifiedCount,
        LocalDate lastDate
) {
}
