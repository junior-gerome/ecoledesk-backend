package com.school.attendance.application.port.in;

import com.school.attendance.domain.model.AttendanceRecord;
import java.util.List;

public interface AttendanceUseCase {
    List<AttendanceRecord> list(AttendanceFilter filter);
    List<AttendanceRecord> saveDailyAttendance(SaveDailyAttendanceCommand command);
    AttendanceRecord updateJustification(Long recordId, String note, boolean justified);
    void delete(Long recordId);
    List<AttendanceSummaryRow> summarize(AttendanceFilter filter);
}
