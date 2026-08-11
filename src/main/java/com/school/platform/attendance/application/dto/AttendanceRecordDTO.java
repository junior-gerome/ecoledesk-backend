package com.school.platform.attendance.application.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AttendanceRecordDTO {
    Long id;
    Long studentId;
    String studentName;
    String firstName;
    String lastName;
    Long classId;
    String className;
    LocalDate date;
    String status;
    Integer hours;
    Boolean justified;
    String justificationNote;
    String updatedAt;
}
