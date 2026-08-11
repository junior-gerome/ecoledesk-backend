package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.student.StudentBasicDTO;
import com.school.platform.enrollment.application.dto.student.StudentFullDTO;
import com.school.platform.enrollment.application.dto.student.StudentMediumDTO;
import com.school.platform.enrollment.domain.model.Student;
import com.school.platform.enrollment.domain.model.StudentGuardian;
import com.school.platform.identityaccess.application.mapper.PersonMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {PersonMapper.class, GuardianProjectionMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentProjectionMapper {

    @Mapping(target = "person", source = "person")
    StudentBasicDTO toBasicDTO(Student entity);

    @Mapping(target = "person", source = "person")
    StudentMediumDTO toMediumDTO(Student entity);

    @Mapping(target = "person", source = "person")
    @Mapping(target = "guardians", expression = "java(mapGuardians(entity))")
    StudentFullDTO toFullDTO(Student entity);

    List<StudentBasicDTO> toBasicDTOList(List<Student> entities);

    List<StudentMediumDTO> toMediumDTOList(List<Student> entities);

    default List<StudentFullDTO.StudentGuardianDTO> mapGuardians(Student entity) {
        if (entity.getStudentGuardians() == null) return List.of();
        return entity.getStudentGuardians().stream()
                .map(sg -> new StudentFullDTO.StudentGuardianDTO(
                        null, // populated by GuardianProjectionMapper if needed
                        sg.getRelationshipType() == null ? null : sg.getRelationshipType().name()))
                .collect(Collectors.toList());
    }
}
