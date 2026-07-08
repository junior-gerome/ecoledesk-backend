package com.school.attendance.adapter.in.rest;

import com.school.attendance.domain.model.AttendanceStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceRequest(
        @NotNull Long classId,
        @NotNull @Size(max = 120) String className,
        @NotNull LocalDate date,
        @NotEmpty List<@Valid Entry> entries
) {
    public record Entry(
            @NotNull Long studentId,
            @NotNull @Size(max = 160) String studentName,
            @NotNull AttendanceStatus status,
            @NotNull BigDecimal hours
    ) {
    }
}
