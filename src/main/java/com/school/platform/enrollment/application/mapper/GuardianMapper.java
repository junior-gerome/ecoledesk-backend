package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.GuardianDTO;
import com.school.platform.enrollment.domain.model.Guardian;
import com.school.platform.identityaccess.domain.model.valueobject.Address;
import com.school.platform.identityaccess.domain.model.valueobject.Email;
import com.school.platform.identityaccess.domain.model.valueobject.PhoneNumber;
import com.school.platform.shared.application.mapper.EntityMapper;
import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GuardianMapper extends EntityMapper<GuardianDTO, Guardian> {

    @Mapping(target = "firstNameGuardian", source = "person.firstName")
    @Mapping(target = "lastNameGuardian", source = "person.lastName")
    @Mapping(target = "email", source = "person.email")
    @Mapping(target = "phoneNumber", source = "person.phone")
    @Mapping(target = "address", source = "person.address")
    GuardianDTO toDto(Guardian entity);

    @Mapping(target = "person.firstName", source = "firstNameGuardian")
    @Mapping(target = "person.lastName", source = "lastNameGuardian")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phone", source = "phoneNumber")
    @Mapping(target = "person.address", source = "address")
    @Mapping(target = "studentGuardians", ignore = true)
    Guardian toEntity(GuardianDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "person.firstName", source = "firstNameGuardian")
    @Mapping(target = "person.lastName", source = "lastNameGuardian")
    @Mapping(target = "person.email", source = "email")
    @Mapping(target = "person.phone", source = "phoneNumber")
    @Mapping(target = "person.address", source = "address")
    @Mapping(target = "studentGuardians", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "active", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(GuardianDTO dto, @MappingTarget Guardian entity);

    List<GuardianDTO> toDto(List<Guardian> entityList);

    List<Guardian> toEntity(List<GuardianDTO> dtoList);

    default Email mapEmail(String value) {
        return value == null || value.isBlank() ? null : Email.of(value);
    }

    default PhoneNumber mapPhoneNumber(String value) {
        return value == null || value.isBlank() ? null : PhoneNumber.of(value);
    }

    default Address mapAddress(String value) {
        return value == null || value.isBlank() ? null : Address.of(value, null, null, null, null);
    }
}
