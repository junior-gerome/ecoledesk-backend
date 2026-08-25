package com.school.platform.staff.application.mapper;

import com.school.platform.staff.application.dto.StaffAssignmentBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberBasicDTO;
import com.school.platform.staff.application.dto.StaffMemberFullDTO;
import com.school.platform.staff.application.dto.StaffMemberMediumDTO;
import com.school.platform.staff.domain.model.StaffAssignment;
import com.school.platform.staff.domain.model.StaffMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StaffMemberMapper {

    @Mapping(target = "employeeNumber", expression = "java(entity.getEmployeeNumber() != null ? entity.getEmployeeNumber().getValue() : null)")
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    @Mapping(target = "photoUrl", source = "person.photoUrl")
    @Mapping(target = "gender", source = "person.gender")
    StaffMemberBasicDTO toBasicDTO(StaffMember entity);

    @Mapping(target = "employeeNumber", expression = "java(entity.getEmployeeNumber() != null ? entity.getEmployeeNumber().getValue() : null)")
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "phone", source = "person.phone")
    @Mapping(target = "gender", source = "person.gender")
    @Mapping(target = "address", source = "person.address")
    @Mapping(target = "photoUrl", source = "person.photoUrl")
    StaffMemberMediumDTO toMediumDTO(StaffMember entity);

    @Mapping(target = "employeeNumber", expression = "java(entity.getEmployeeNumber() != null ? entity.getEmployeeNumber().getValue() : null)")
    @Mapping(target = "firstName", source = "person.firstName")
    @Mapping(target = "lastName", source = "person.lastName")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "phone", source = "person.phone")
    @Mapping(target = "gender", source = "person.gender")
    @Mapping(target = "birthDate", source = "person.birthDate")
    @Mapping(target = "address", source = "person.address")
    @Mapping(target = "city", source = "person.city")
    @Mapping(target = "country", source = "person.country")
    @Mapping(target = "photoUrl", source = "person.photoUrl")
    @Mapping(target = "assignments", source = "entity", qualifiedByName = "toAssignmentList")
    StaffMemberFullDTO toFullDTO(StaffMember entity);

    List<StaffMemberBasicDTO> toBasicDTOList(List<StaffMember> entities);

    List<StaffMemberMediumDTO> toMediumDTOList(List<StaffMember> entities);

    List<StaffMemberFullDTO> toFullDTOList(List<StaffMember> entities);

    @org.mapstruct.Named("toAssignmentList")
    default List<StaffAssignmentBasicDTO> toAssignmentList(StaffMember entity) {
        return null; // populated by service layer via StaffAssignmentMapper
    }
}
