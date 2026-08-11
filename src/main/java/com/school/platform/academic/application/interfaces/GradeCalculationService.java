package com.school.platform.academic.application.interfaces;

import java.util.Map;

public interface GradeCalculationService {
    Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId, String period);
    Map<String, Object> getClassGradeStats(Long classId, Long subjectId, String period);
    Map<String, Object> getStudentReport(Long studentId, String period);
    Integer calculateStudentRank(Long studentId, Long classId);
}
