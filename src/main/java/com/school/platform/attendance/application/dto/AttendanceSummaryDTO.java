package com.school.platform.attendance.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AttendanceSummaryDTO {
    Long studentId;
    String studentName;
    String className;
    long totalAbsences;
    long totalLates;
    long unjustifiedCount;
    LocalDate lastDate;
}
