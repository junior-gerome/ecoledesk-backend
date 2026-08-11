package com.school.platform.staff.application.interfaces;

import com.school.platform.staff.application.dto.position.StaffPositionDTO;

import java.util.List;

public interface StaffPositionService {
    List<StaffPositionDTO> getAllPositions();
}
