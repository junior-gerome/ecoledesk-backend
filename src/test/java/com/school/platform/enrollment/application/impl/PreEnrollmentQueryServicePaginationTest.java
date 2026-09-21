package com.school.platform.enrollment.application.impl;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.school.platform.enrollment.application.mapper.PreEnrollmentProjectionMapper;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.PreEnrollmentRepository;

class PreEnrollmentQueryServicePaginationTest {

    private final PreEnrollmentRepository preEnrollmentRepository = mock(PreEnrollmentRepository.class);
    private final PreEnrollmentProjectionMapper mapper = mock(PreEnrollmentProjectionMapper.class);
    private final PreEnrollmentQueryServiceImpl service =
            new PreEnrollmentQueryServiceImpl(preEnrollmentRepository, mapper);

    @Test
    void getAllNormalizesTrimmedSearchBeforeServerSideEvaluation() {
        Pageable pageable = PageRequest.of(0, 20);
        when(preEnrollmentRepository.findAllWithAcademicYearAndSearch(eq("ada"), isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.getAll("  ada  ", null, pageable);

        verify(preEnrollmentRepository).findAllWithAcademicYearAndSearch(eq("ada"), isNull(), eq(pageable));
    }

    @Test
    void getAllForwardsBlankSearchAsNullAndKeepsStatus() {
        Pageable pageable = PageRequest.of(1, 20);
        when(preEnrollmentRepository.findAllWithAcademicYearAndSearch(
                        isNull(), eq(PreEnrollmentStatus.SUBMITTED), eq(pageable)))
                .thenReturn(new PageImpl<>(List.<PreEnrollment>of(), pageable, 0));

        service.getAll("   ", PreEnrollmentStatus.SUBMITTED, pageable);

        verify(preEnrollmentRepository)
                .findAllWithAcademicYearAndSearch(isNull(), eq(PreEnrollmentStatus.SUBMITTED), eq(pageable));
    }
}