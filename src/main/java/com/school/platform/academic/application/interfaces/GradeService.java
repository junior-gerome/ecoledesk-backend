package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.grade.BulkGradeCreateRequest;
import com.school.platform.academic.application.dto.grade.GradeBatchStatusRequest;
import com.school.platform.academic.application.dto.grade.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.grade.GradeResponseDTO;
import com.school.platform.reporting.application.dto.ClassPerformanceReport;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GradeService {
    Page<GradeResponseDTO> findByClassIdAndPeriod(Long classId, String period, Pageable pageable);
    Page<GradeResponseDTO> findByStudentId(Long studentId, Pageable pageable);
    GradeResponseDTO save(GradeCreateRequestDTO dto);
    GradeResponseDTO createGrade(GradeCreateRequestDTO dto);
    GradeResponseDTO updateGrade(Long id, GradeCreateRequestDTO dto);
    void deleteGrade(Long id);
    List<GradeResponseDTO> addBulkGrades(BulkGradeCreateRequest request);
    GradeResponseDTO validateGrade(Long id);
    List<GradeResponseDTO> validateGradesByClass(GradeBatchStatusRequest request);
    List<GradeResponseDTO> lockGradesByClass(GradeBatchStatusRequest request);
    List<GradeResponseDTO> unlockGradesByClass(GradeBatchStatusRequest request);
    List<GradeResponseDTO> addBulkGrades(List<GradeCreateRequestDTO> grades);
    BigDecimal calculateGeneralAverage(Long studentId);
    Integer calculateStudentRank(Long studentId, Long classId);
    List<GradeResponseDTO> getGradesByStudent(Long studentId);
    GradeResponseDTO getGradeById(Long id);
    List<GradeResponseDTO> getGradesBySubjectAndSequence(Long subjectId, Long sequenceId);
    Map<String, Object> getStudentGradeStats(Long studentId, Long subjectId, String period);
    Map<String, Object> getClassGradeStats(Long classId, Long subjectId, String period);
    Map<String, Object> getStudentReport(Long studentId, String period);
    ClassPerformanceReport generateClassPerformanceReport(Long classId);
    ClassPerformanceReport generateClassPerformanceReport(Long classId, String period);
    void clearGradesCache();
}
