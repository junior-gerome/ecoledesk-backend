package com.school.platform.academic.application;

import com.school.platform.reporting.application.dto.ClassPerformanceReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeStatisticsService {

    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public ClassPerformanceReport generateClassPerformanceReport(Long classId) {
        return gradeService.generateClassPerformanceReport(classId);
    }

    @Transactional(readOnly = true)
    public ClassPerformanceReport generateClassPerformanceReport(Long classId, String period) {
        return gradeService.generateClassPerformanceReport(classId, period);
    }
}
