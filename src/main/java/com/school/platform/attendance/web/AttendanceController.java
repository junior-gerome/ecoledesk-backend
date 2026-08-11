package com.school.platform.attendance.web;

import com.school.platform.attendance.application.AttendanceService;
import com.school.platform.attendance.application.dto.AttendanceRecordDTO;
import com.school.platform.attendance.application.dto.AttendanceSummaryDTO;
import com.school.platform.attendance.application.dto.DailyAttendanceRequest;
import com.school.platform.attendance.application.dto.JustificationRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AttendanceRecordDTO>> getRecords(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, name = "status") String statuses,
            @RequestParam(required = false) Boolean justified,
            @RequestParam(required = false) Long academicYearId) {
        return ResponseEntity.ok(
                attendanceService.getRecords(classId, date, dateFrom, dateTo, statuses, justified, academicYearId));
    }

    @PostMapping("/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AttendanceRecordDTO>> saveDaily(@RequestBody DailyAttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.saveDaily(request));
    }

    @PatchMapping("/records/{id}/justification")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<AttendanceRecordDTO> updateJustification(
            @PathVariable Long id,
            @RequestBody JustificationRequest request) {
        return ResponseEntity.ok(attendanceService.updateJustification(id, request));
    }

    @DeleteMapping("/records/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        attendanceService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<AttendanceSummaryDTO>> getSummary(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(attendanceService.getSummary(classId, dateFrom, dateTo));
    }
}
