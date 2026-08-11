package com.school.platform.staff.application.impl;

import com.school.platform.staff.application.dto.position.StaffPositionDTO;
import com.school.platform.staff.application.interfaces.StaffPositionService;
import com.school.platform.staff.domain.model.StaffPosition;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StaffPositionServiceImpl implements StaffPositionService {

    @Override
    public List<StaffPositionDTO> getAllPositions() {
        return Arrays.stream(StaffPosition.values())
                .map(StaffPositionDTO::of)
                .toList();
    }
}
