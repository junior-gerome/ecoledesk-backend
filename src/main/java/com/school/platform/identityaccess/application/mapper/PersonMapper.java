package com.school.platform.identityaccess.application.mapper;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import com.school.platform.identityaccess.application.dto.person.PersonFullDTO;
import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import com.school.platform.identityaccess.domain.model.Person;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true), unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

    @Mapping(target = "email", expression = "java(person.getEmail())")
    @Mapping(target = "phone", expression = "java(person.getPhone())")
    PersonBasicDTO toBasicDTO(Person person);

    @Mapping(target = "email", expression = "java(person.getEmail())")
    @Mapping(target = "phone", expression = "java(person.getPhone())")
    @Mapping(target = "birthDate", expression = "java(person.getBirthDate())")
    @Mapping(target = "address", expression = "java(person.getAddress())")
    @Mapping(target = "city", expression = "java(person.getCity())")
    PersonMediumDTO toMediumDTO(Person person);

    @Mapping(target = "email", expression = "java(person.getEmail())")
    @Mapping(target = "phone", expression = "java(person.getPhone())")
    @Mapping(target = "birthDate", expression = "java(person.getBirthDate())")
    @Mapping(target = "address", expression = "java(person.getAddress())")
    @Mapping(target = "city", expression = "java(person.getCity())")
    @Mapping(target = "country", expression = "java(person.getCountry())")
    PersonFullDTO toFullDTO(Person person);

    default Person toEntity(PersonFullDTO dto) {
        if (dto == null) {
            return null;
        }
        Person person = new Person();
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        person.setPhone(dto.getPhone());
        person.setBirthDate(dto.getBirthDate());
        person.setGender(dto.getGender());
        person.setPhotoUrl(dto.getPhotoUrl());
        person.setAddress(dto.getAddress());
        person.setCity(dto.getCity());
        person.setCountry(dto.getCountry());
        return person;
    }

    default void updateEntity(PersonFullDTO dto, @MappingTarget Person person) {
        if (dto == null || person == null) {
            return;
        }
        person.setFirstName(dto.getFirstName());
        person.setLastName(dto.getLastName());
        person.setEmail(dto.getEmail());
        person.setPhone(dto.getPhone());
        person.setBirthDate(dto.getBirthDate());
        person.setGender(dto.getGender());
        person.setPhotoUrl(dto.getPhotoUrl());
        person.setAddress(dto.getAddress());
        person.setCity(dto.getCity());
        person.setCountry(dto.getCountry());
    }

    List<PersonBasicDTO> toBasicDTOList(List<Person> persons);

    List<PersonMediumDTO> toMediumDTOList(List<Person> persons);
}
