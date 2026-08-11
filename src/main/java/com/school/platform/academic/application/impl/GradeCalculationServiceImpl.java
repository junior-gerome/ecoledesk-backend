package com.school.platform.academic.application.impl;

import java.util.Map;
import com.school.platform.academic.application.interfaces.GradeCalculationService;
import com.school.platform.academic.application.interfaces.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeCalculationServiceImpl implements GradeCalculationService {

    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId, String period) {
        return gradeService.getStudentGradeStats(studentId, subjectId, period);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getClassGradeStats(Long classId, Long subjectId, String period) {
        return gradeService.getClassGradeStats(classId, subjectId, period);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentReport(Long studentId, String period) {
        return gradeService.getStudentReport(studentId, period);
    }

    @Transactional(readOnly = true)
    public Integer calculateStudentRank(Long studentId, Long classId) {
        return gradeService.calculateStudentRank(studentId, classId);
    }
}
