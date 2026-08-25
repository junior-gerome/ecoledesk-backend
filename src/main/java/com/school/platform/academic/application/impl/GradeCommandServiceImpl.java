package com.school.platform.academic.application.impl;

import com.school.platform.academic.application.dto.grade.BulkGradeCreateRequest;
import com.school.platform.academic.application.dto.grade.GradeBatchStatusRequest;
import com.school.platform.academic.application.dto.grade.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.grade.GradeResponseDTO;
import com.school.platform.academic.application.interfaces.GradeCommandService;
import com.school.platform.academic.application.interfaces.GradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeCommandServiceImpl implements GradeCommandService {

    private final GradeService gradeService;

    @Transactional
    public GradeResponseDTO saveGrade(GradeCreateRequestDTO dto) {
        return gradeService.save(dto);
    }

    @Transactional
    public GradeResponseDTO updateGrade(Long id, GradeCreateRequestDTO dto) {
        return gradeService.updateGrade(id, dto);
    }

    @Transactional
    public void deleteGrade(Long id) {
        gradeService.deleteGrade(id);
    }

    @Transactional
    public List<GradeResponseDTO> saveBulkGrades(List<GradeCreateRequestDTO> grades) {
        return gradeService.addBulkGrades(grades);
    }

    @Transactional
    public List<GradeResponseDTO> saveBulkGrades(BulkGradeCreateRequest request) {
        return gradeService.addBulkGrades(request);
    }

    @Transactional
    public GradeResponseDTO validateGrade(Long id) {
        return gradeService.validateGrade(id);
    }

    @Transactional
    public List<GradeResponseDTO> validateGradesByClass(GradeBatchStatusRequest request) {
        return gradeService.validateGradesByClass(request);
    }

    @Transactional
    public List<GradeResponseDTO> lockGradesByClass(GradeBatchStatusRequest request) {
        return gradeService.lockGradesByClass(request);
    }

    @Transactional
    public List<GradeResponseDTO> unlockGradesByClass(GradeBatchStatusRequest request) {
        return gradeService.unlockGradesByClass(request);
    }
}
