package com.school.platform.academic.application;

import com.school.platform.academic.application.dto.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.GradeResponseDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeCommandService {

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
}
