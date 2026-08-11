package com.school.platform.staff.application.interfaces;

import com.school.platform.staff.application.dto.StaffAssignmentCreateRequest;
import com.school.platform.staff.application.dto.StaffAssignmentFullDTO;
import com.school.platform.staff.application.dto.StaffAssignmentMediumDTO;
import com.school.platform.staff.domain.model.StaffPosition;

import java.util.List;

public interface StaffAssignmentService {

    List<StaffAssignmentMediumDTO> getByStaffMember(Long staffMemberId);

    List<StaffAssignmentMediumDTO> getActiveByStaffMember(Long staffMemberId);

    List<StaffAssignmentMediumDTO> getByPosition(StaffPosition position);

    StaffAssignmentFullDTO getById(Long id);

    StaffAssignmentFullDTO create(StaffAssignmentCreateRequest request);

    StaffAssignmentFullDTO closeAssignment(Long id);
}
