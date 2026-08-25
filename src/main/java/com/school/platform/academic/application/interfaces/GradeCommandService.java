package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.grade.BulkGradeCreateRequest;
import com.school.platform.academic.application.dto.grade.GradeBatchStatusRequest;
import com.school.platform.academic.application.dto.grade.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.grade.GradeResponseDTO;
import java.util.List;

public interface GradeCommandService {
    GradeResponseDTO saveGrade(GradeCreateRequestDTO dto);
    GradeResponseDTO updateGrade(Long id, GradeCreateRequestDTO dto);
    void deleteGrade(Long id);
    List<GradeResponseDTO> saveBulkGrades(List<GradeCreateRequestDTO> grades);
    List<GradeResponseDTO> saveBulkGrades(BulkGradeCreateRequest request);
    GradeResponseDTO validateGrade(Long id);
    List<GradeResponseDTO> validateGradesByClass(GradeBatchStatusRequest request);
    List<GradeResponseDTO> lockGradesByClass(GradeBatchStatusRequest request);
    List<GradeResponseDTO> unlockGradesByClass(GradeBatchStatusRequest request);
}
