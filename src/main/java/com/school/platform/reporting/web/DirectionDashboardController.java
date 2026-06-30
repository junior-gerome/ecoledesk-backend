package com.school.platform.reporting.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.school.platform.reporting.application.dto.dashboard.DirectionDashboardDTO;
import com.school.platform.reporting.application.DirectionDashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DirectionDashboardController {
    private final DirectionDashboardService directionDashboardService;

    @GetMapping("/direction")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    public ResponseEntity<DirectionDashboardDTO> getDirectionSummary() {
        return ResponseEntity.ok(directionDashboardService.getSummary());
    }
}
