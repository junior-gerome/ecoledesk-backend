package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.subject.SubjectReponse;
import com.school.platform.academic.application.dto.subject.SubjectRequest;
import java.util.List;

public interface SubjectService {
    SubjectRequest createdSubject(SubjectRequest dto);
    SubjectReponse updatedSubject(Long id, SubjectReponse dto);
    List<SubjectReponse> getAllSubject();
    long getTotalSubjects();
    void deleteSubject(Long id);
    SubjectReponse getSubjectById(Long id);
    String generateSubjectCode(String nameSubject);

}
