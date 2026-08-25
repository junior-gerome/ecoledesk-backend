package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.grade.GradeResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GradeQueryService {
    Page<GradeResponseDTO> findByClassIdAndPeriod(Long classId, String period, Pageable pageable);
    Page<GradeResponseDTO> findByStudentId(Long studentId, Pageable pageable);
    GradeResponseDTO getGradeById(Long id);
    List<GradeResponseDTO> getGradesByStudent(Long studentId);
    List<GradeResponseDTO> getGradesBySubjectAndSequence(Long subjectId, Long sequenceId);
}
