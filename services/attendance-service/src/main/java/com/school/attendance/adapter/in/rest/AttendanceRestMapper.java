package com.school.attendance.adapter.in.rest;

import com.school.attendance.application.port.in.DailyAttendanceEntry;
import com.school.attendance.application.port.in.SaveDailyAttendanceCommand;
import com.school.attendance.domain.model.AttendanceRecord;

final class AttendanceRestMapper {
    private AttendanceRestMapper() {
    }

    static SaveDailyAttendanceCommand toCommand(DailyAttendanceRequest request) {
        return new SaveDailyAttendanceCommand(request.classId(), request.className(), request.date(),
                request.entries().stream()
                        .map(entry -> new DailyAttendanceEntry(entry.studentId(), entry.studentName(),
                                entry.status(), entry.hours()))
                        .toList());
    }

    static AttendanceResponse toResponse(AttendanceRecord record) {
        return new AttendanceResponse(record.id(), record.studentId(), record.studentName(), record.classId(),
                record.className(), record.date(), record.status(), record.hours(), record.justified(),
                record.justificationNote(), record.updatedAt());
    }
}
