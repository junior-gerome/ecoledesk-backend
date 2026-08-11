package com.school.platform.academic.application.interfaces;

import com.school.platform.academic.application.dto.TeacherDTO;
import java.util.List;

public interface TeacherService {
    List<TeacherDTO> getAllEnseignants();
    TeacherDTO getEnseignantById(Long id);
    TeacherDTO createEnseignant(TeacherDTO dto);
    TeacherDTO updateEnseignant(Long id, TeacherDTO dto);
    void deleteEnseignant(Long id);
    long getTotalTeachers();
}
