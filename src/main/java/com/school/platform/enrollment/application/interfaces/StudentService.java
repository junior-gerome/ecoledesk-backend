package com.school.platform.enrollment.application.interfaces;

import com.school.platform.enrollment.application.dto.StudentDTO;
import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import com.school.platform.enrollment.application.dto.student.StudentFullDTO;
import com.school.platform.enrollment.application.dto.student.StudentMediumDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface StudentService {

    // Legacy (rétrocompatibilité)
    StudentDTO createStudentWithGuardian(StudentDTO dto);
    StudentDTO updateStudent(Long id, StudentDTO dto);
    void deleteStudent(Long id);
    StudentDTO linkGuardian(Long studentId, Long guardianId);

    // Projections
    List<StudentBasicDTO> getAllBasic();
    Page<StudentMediumDTO> getAllMedium(Pageable pageable);
    StudentFullDTO getById(Long id);
    Page<StudentMediumDTO> getByClassroom(Long classroomId, Pageable pageable);
    long count();
    Map<String, Object> getStatistics();
}
