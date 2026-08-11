package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.enrollment.EnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.enrollment.EnrollmentMediumDTO;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {StudentProjectionMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EnrollmentProjectionMapper {

    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "studentId", source = "student.id")
    @Mapping(target = "studentName", expression = "java(entity.getStudent() == null || entity.getStudent().getPerson() == null ? null : entity.getStudent().getPerson().getFirstName() + \" \" + entity.getStudent().getPerson().getLastName())")
    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomName", source = "classroom.nameClasse")
    EnrollmentBasicDTO toBasicDTO(Enrollment entity);

    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "student", source = "student")
    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomName", source = "classroom.nameClasse")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.libelleAcademicYear")
    EnrollmentMediumDTO toMediumDTO(Enrollment entity);

    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "student", source = "student")
    @Mapping(target = "preEnrollmentId", source = "preEnrollment.id")
    @Mapping(target = "preEnrollmentNumber", source = "preEnrollment.number.value")
    @Mapping(target = "classroomId", source = "classroom.id")
    @Mapping(target = "classroomName", source = "classroom.nameClasse")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.libelleAcademicYear")
    EnrollmentFullDTO toFullDTO(Enrollment entity);

    List<EnrollmentBasicDTO> toBasicDTOList(List<Enrollment> entities);

    List<EnrollmentMediumDTO> toMediumDTOList(List<Enrollment> entities);

    List<EnrollmentFullDTO> toFullDTOList(List<Enrollment> entities);
}
