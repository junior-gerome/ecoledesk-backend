package com.school.attendance.application.port.out;

public record ClassRosterStudent(
        Long studentId,
        String studentName,
        Long classId,
        String className
) {
}
