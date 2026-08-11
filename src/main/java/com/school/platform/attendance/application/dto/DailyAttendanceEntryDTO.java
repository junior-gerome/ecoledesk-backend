package com.school.platform.attendance.application.dto;

import lombok.Data;

@Data
public class DailyAttendanceEntryDTO {
    private Long studentId;
    private String status;
    private Integer hours;
}
