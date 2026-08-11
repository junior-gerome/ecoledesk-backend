package com.school.platform.attendance.application;

import com.school.platform.attendance.application.dto.AttendanceRecordDTO;
import com.school.platform.attendance.application.dto.AttendanceSummaryDTO;
import com.school.platform.attendance.application.dto.DailyAttendanceRequest;
import com.school.platform.attendance.application.dto.JustificationRequest;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendanceRecordDTO> getRecords(
            Long classId,
            LocalDate date,
            LocalDate dateFrom,
            LocalDate dateTo,
            String statuses,
            Boolean justified,
            Long academicYearId);

    List<AttendanceRecordDTO> saveDaily(DailyAttendanceRequest request);

    AttendanceRecordDTO updateJustification(Long id, JustificationRequest request);

    void deleteRecord(Long id);

    List<AttendanceSummaryDTO> getSummary(Long classId, LocalDate dateFrom, LocalDate dateTo);
}
