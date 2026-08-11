package com.school.platform.enrollment.application.interfaces;

import com.school.platform.enrollment.application.dto.enrollment.EnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentMediumDTO;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentQueryService {

    EnrollmentFullDTO getById(Long id);

    Page<EnrollmentMediumDTO> getAll(Pageable pageable);

    List<EnrollmentBasicDTO> getByClassroom(Long classroomId, EnrollmentStatus status);

    List<EnrollmentBasicDTO> getByStatus(EnrollmentStatus status);

    long countByStatus(EnrollmentStatus status);
}
