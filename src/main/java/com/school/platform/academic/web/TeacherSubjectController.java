package com.school.platform.academic.web;

import java.util.List;

import com.school.platform.academic.application.dto.teacher.TeacherSubjectResponse;
import com.school.platform.academic.application.dto.teacher.TeacherScheduleResponse;
import com.school.platform.academic.application.interfaces.AffectationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
public class TeacherSubjectController {

    private final AffectationService affectationService;

    @GetMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<TeacherSubjectResponse>> getSubjects(
            @RequestParam(value = "teacherId", required = false) Long teacherId) {
        return ResponseEntity.ok(affectationService.getTeacherSubjects(teacherId));
    }

    @GetMapping("/schedule")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT') or hasRole('ENSEIGNANT')")
    public ResponseEntity<List<TeacherScheduleResponse>> getSchedule(
            @RequestParam(value = "teacherId", required = false) Long teacherId) {
        return ResponseEntity.ok(affectationService.getTeacherSchedule(teacherId));
    }
}