package com.school.attendance.application.port.in;

import java.time.LocalDate;
import java.util.List;

public record SaveDailyAttendanceCommand(
        Long classId,
        String className,
        LocalDate date,
        List<DailyAttendanceEntry> entries
) {
}
