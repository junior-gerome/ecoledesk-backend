package com.school.platform.enrollment.application.impl;

import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentMediumDTO;
import com.school.platform.enrollment.application.interfaces.PreEnrollmentQueryService;
import com.school.platform.enrollment.application.mapper.PreEnrollmentProjectionMapper;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;
import com.school.platform.shared.domain.exception.shared.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PreEnrollmentQueryServiceImpl implements PreEnrollmentQueryService {

    private final PreEnrollmentRepository preEnrollmentRepository;
    private final PreEnrollmentProjectionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public PreEnrollmentFullDTO getById(Long id) {
        PreEnrollment pre = preEnrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PreEnrollment", "id", id));
        return mapper.toFullDTO(pre);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PreEnrollmentMediumDTO> getAll(Pageable pageable) {
        // JOIN FETCH academicYear pour éviter LazyInitializationException
        // lors de l'accès à academicYear.libelleAcademicYear dans le mapper.
        return preEnrollmentRepository.findAllWithAcademicYear(pageable).map(mapper::toMediumDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PreEnrollmentMediumDTO> getAll(PreEnrollmentStatus status, Pageable pageable) {
        // Filtre par statut avec JOIN FETCH pour éviter LazyInitializationException.
        return preEnrollmentRepository.findAllWithAcademicYearByStatus(status, pageable)
                .map(mapper::toMediumDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreEnrollmentBasicDTO> getByStatus(PreEnrollmentStatus status) {
        return mapper.toBasicDTOList(preEnrollmentRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PreEnrollmentBasicDTO> getByAcademicYear(Long academicYearId) {
        return mapper.toBasicDTOList(preEnrollmentRepository.findByAcademicYearId(academicYearId));
    }
}
