package com.school.platform.attendance.application.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class DailyAttendanceRequest {
    private Long classId;
    private LocalDate date;
    private List<DailyAttendanceEntryDTO> entries;
}
