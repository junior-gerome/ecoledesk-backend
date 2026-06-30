package com.school.attendance.application.port.in;

import com.school.attendance.domain.model.AttendanceStatus;
import java.time.LocalDate;
import java.util.Set;

public record AttendanceFilter(
        Long classId,
        LocalDate date,
        LocalDate dateFrom,
        LocalDate dateTo,
        Set<AttendanceStatus> statuses,
        Boolean justified,
        Long schoolYearId
) {
    public AttendanceFilter(
            Long classId,
            LocalDate date,
            LocalDate dateFrom,
            LocalDate dateTo,
            Set<AttendanceStatus> statuses,
            Boolean justified
    ) {
        this(classId, date, dateFrom, dateTo, statuses, justified, null);
    }
}
