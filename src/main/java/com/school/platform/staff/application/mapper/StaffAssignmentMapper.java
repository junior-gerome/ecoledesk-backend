package com.school.platform.staff.application.mapper;

import com.school.platform.staff.application.dto.StaffAssignmentBasicDTO;
import com.school.platform.staff.application.dto.StaffAssignmentFullDTO;
import com.school.platform.staff.application.dto.StaffAssignmentMediumDTO;
import com.school.platform.staff.domain.model.StaffAssignment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StaffMemberMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffAssignmentMapper {

    StaffAssignmentBasicDTO toBasicDTO(StaffAssignment entity);

    @Mapping(target = "staffMemberId", source = "staffMember.id")
    @Mapping(target = "staffMemberName", expression = "java(entity.getStaffMember().getPerson().getFirstName() + \" \" + entity.getStaffMember().getPerson().getLastName())")
    StaffAssignmentMediumDTO toMediumDTO(StaffAssignment entity);

    @Mapping(target = "staffMember", source = "staffMember")
    StaffAssignmentFullDTO toFullDTO(StaffAssignment entity);

    List<StaffAssignmentBasicDTO> toBasicDTOList(List<StaffAssignment> entities);

    List<StaffAssignmentMediumDTO> toMediumDTOList(List<StaffAssignment> entities);

    List<StaffAssignmentFullDTO> toFullDTOList(List<StaffAssignment> entities);
}
