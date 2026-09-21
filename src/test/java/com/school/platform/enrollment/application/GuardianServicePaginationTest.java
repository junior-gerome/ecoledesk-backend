package com.school.platform.enrollment.application;

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

import com.school.platform.enrollment.application.mapper.GuardianMapper;
import com.school.platform.enrollment.application.mapper.GuardianProjectionMapper;
import com.school.platform.enrollment.infrastructure.persistence.GuardianRepository;

class GuardianServicePaginationTest {

    private final GuardianRepository guardianRepository = mock(GuardianRepository.class);
    private final GuardianMapper mapper = mock(GuardianMapper.class);
    private final GuardianProjectionMapper projectionMapper = mock(GuardianProjectionMapper.class);
    private final GuardianService service = new GuardianService(guardianRepository, mapper, projectionMapper);

    @Test
    void getAllGuardiansWithSearchNormalizesThenForwardsToFindWithSearch() {
        Pageable pageable = PageRequest.of(2, 10);
        when(guardianRepository.findWithSearch(eq("Marie"), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.getAllGuardians("  Marie  ", pageable);

        verify(guardianRepository).findWithSearch(eq("Marie"), eq(pageable));
    }

    @Test
    void getAllGuardiansWithBlankSearchForwardsNull() {
        Pageable pageable = PageRequest.of(0, 20);
        when(guardianRepository.findWithSearch(isNull(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        service.getAllGuardians("   ", pageable);

        verify(guardianRepository).findWithSearch(isNull(), eq(pageable));
    }
}