package com.school.platform.enrollment.application.impl;

import com.school.platform.enrollment.application.dto.enrollment.EnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentMediumDTO;
import com.school.platform.enrollment.application.interfaces.EnrollmentQueryService;
import com.school.platform.enrollment.application.mapper.EnrollmentProjectionMapper;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.shared.domain.exception.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentQueryServiceImpl implements EnrollmentQueryService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentProjectionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public EnrollmentFullDTO getById(Long id) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment", "id", id));
        return mapper.toFullDTO(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EnrollmentMediumDTO> getAll(Pageable pageable) {
        return enrollmentRepository.findAll(pageable).map(mapper::toMediumDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentBasicDTO> getByClassroom(Long classroomId, EnrollmentStatus status) {
        return mapper.toBasicDTOList(
                enrollmentRepository.findByClassroomIdAndStatus(classroomId, status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentBasicDTO> getByStatus(EnrollmentStatus status) {
        return mapper.toBasicDTOList(enrollmentRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(EnrollmentStatus status) {
        return enrollmentRepository.countByStatus(status);
    }
}
