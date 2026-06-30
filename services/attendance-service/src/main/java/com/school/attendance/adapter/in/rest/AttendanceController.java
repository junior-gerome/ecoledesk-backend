package com.school.attendance.adapter.in.rest;

import com.school.attendance.application.port.in.AttendanceFilter;
import com.school.attendance.application.port.in.AttendanceSummaryRow;
import com.school.attendance.application.port.in.AttendanceUseCase;
import com.school.attendance.domain.model.AttendanceStatus;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {
    private final AttendanceUseCase useCase;

    public AttendanceController(AttendanceUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/records")
    public List<AttendanceResponse> records(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, name = "status") String statuses,
            @RequestParam(required = false) Boolean justified,
            @RequestParam(required = false) Long anneeScolaireId
    ) {
        return useCase.list(new AttendanceFilter(
                        classId,
                        date,
                        dateFrom,
                        dateTo,
                        parseStatuses(statuses),
                        justified,
                        anneeScolaireId))
                .stream()
                .map(AttendanceRestMapper::toResponse)
                .toList();
    }

    @PostMapping("/daily")
    public List<AttendanceResponse> saveDaily(@Valid @RequestBody DailyAttendanceRequest request) {
        return useCase.saveDailyAttendance(AttendanceRestMapper.toCommand(request)).stream()
                .map(AttendanceRestMapper::toResponse)
                .toList();
    }

    @PatchMapping("/records/{id}/justification")
    public AttendanceResponse justify(@PathVariable Long id, @Valid @RequestBody JustificationRequest request) {
        return AttendanceRestMapper.toResponse(
                useCase.updateJustification(id, request.justificationNote(), request.justified()));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public List<AttendanceSummaryRow> summary(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return useCase.summarize(new AttendanceFilter(classId, null, dateFrom, dateTo, null, null));
    }

    private Set<AttendanceStatus> parseStatuses(String statuses) {
        if (statuses == null || statuses.isBlank()) {
            return null;
        }
        return Arrays.stream(statuses.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(AttendanceStatus::valueOf)
                .collect(Collectors.toSet());
    }
}
