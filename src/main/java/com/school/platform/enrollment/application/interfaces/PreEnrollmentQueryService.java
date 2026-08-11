package com.school.platform.enrollment.application.interfaces;

import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentMediumDTO;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PreEnrollmentQueryService {

    PreEnrollmentFullDTO getById(Long id);

    Page<PreEnrollmentMediumDTO> getAll(Pageable pageable);

    List<PreEnrollmentBasicDTO> getByStatus(PreEnrollmentStatus status);

    List<PreEnrollmentBasicDTO> getByAcademicYear(Long academicYearId);
}
