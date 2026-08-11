package com.school.platform.staff.application.interfaces;

import com.school.platform.staff.domain.model.StaffMember;

import java.util.List;

public interface ITeachingStaffService {

    StaffMember getTeachingStaffMember(Long staffMemberId);

    List<StaffMember> getTeachingStaffMembers();
}
