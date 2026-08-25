package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.subject.SubjectDTO;
import java.util.List;

public interface SubjectService {
    SubjectDTO createdSubject(SubjectDTO dto);
    SubjectDTO updatedSubject(Long id, SubjectDTO dto);
    List<SubjectDTO> getAllSubject();
    long getTotalSubjects();
    void deleteSubject(Long id);
    SubjectDTO getSubjectById(Long id);
}
