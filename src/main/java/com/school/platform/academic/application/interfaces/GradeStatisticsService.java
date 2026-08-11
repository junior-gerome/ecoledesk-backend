package com.school.platform.academic.application.interfaces;

import com.school.platform.reporting.application.dto.ClassPerformanceReport;

public interface GradeStatisticsService {
    ClassPerformanceReport generateClassPerformanceReport(Long classId);
    ClassPerformanceReport generateClassPerformanceReport(Long classId, String period);
}
