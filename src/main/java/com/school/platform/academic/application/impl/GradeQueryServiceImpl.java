package com.school.platform.academic.application.impl;

import com.school.platform.academic.application.dto.grade.GradeResponseDTO;
import com.school.platform.academic.application.interfaces.GradeQueryService;
import com.school.platform.academic.application.interfaces.GradeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GradeQueryServiceImpl implements GradeQueryService {

    private final GradeService gradeService;

    @Transactional(readOnly = true)
    public Page<GradeResponseDTO> findByClassIdAndPeriod(Long classId, String period, Pageable pageable) {
        return gradeService.findByClassIdAndPeriod(classId, period, pageable);
    }

    @Transactional(readOnly = true)
    public Page<GradeResponseDTO> findByStudentId(Long studentId, Pageable pageable) {
        return gradeService.findByStudentId(studentId, pageable);
    }

    @Transactional(readOnly = true)
    public GradeResponseDTO getGradeById(Long id) {
        return gradeService.getGradeById(id);
    }

    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesByStudent(Long studentId) {
        return gradeService.getGradesByStudent(studentId);
    }

    @Transactional(readOnly = true)
    public List<GradeResponseDTO> getGradesBySubjectAndSequence(Long subjectId, Long sequenceId) {
        return gradeService.getGradesBySubjectAndSequence(subjectId, sequenceId);
    }
}
