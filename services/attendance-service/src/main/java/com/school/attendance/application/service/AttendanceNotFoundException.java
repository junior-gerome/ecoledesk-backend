package com.school.attendance.application.service;

public class AttendanceNotFoundException extends RuntimeException {
    public AttendanceNotFoundException(Long id) {
        super("Attendance record not found with id " + id);
    }
}
