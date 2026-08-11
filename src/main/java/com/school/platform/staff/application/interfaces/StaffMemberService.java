package com.school.platform.staff.application.interfaces;

import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberCreateRequest;
import com.school.platform.staff.application.dto.StaffMemberFullDTO;
import com.school.platform.staff.application.dto.StaffMemberMediumDTO;

import java.util.List;

public interface StaffMemberService {

    List<StaffMemberBasicDTO> getAllBasic();

    List<StaffMemberMediumDTO> getAllMedium();

    StaffMemberFullDTO getById(Long id);

    StaffMemberFullDTO create(StaffMemberCreateRequest request);

    StaffMemberFullDTO update(Long id, StaffMemberCreateRequest request);

    void deactivate(Long id);

    long count();
}
