package com.school.platform.academic.application.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.school.platform.academic.application.dto.GradeCreateRequestDTO;
import com.school.platform.academic.application.dto.GradeResponseDTO;
import com.school.platform.academic.domain.model.Grade;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    @Mapping(source = "score", target = "grade")
    @Mapping(source = "comments", target = "pedagogicalComment")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "sequence", ignore = true)
    @Mapping(target = "trimestre", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    Grade toEntity(GradeCreateRequestDTO dto);

    @Mapping(source = "student.id", target = "studentId")
    @Mapping(target = "studentName", expression = "java(buildStudentName(entity))")
    @Mapping(source = "subject.id", target = "subjectId")
    @Mapping(target = "subjectName", expression = "java(buildSubjectName(entity))")
    @Mapping(source = "classe.id", target = "classeId")
    @Mapping(target = "classeName", expression = "java(buildClasseName(entity))")
    @Mapping(source = "grade", target = "score")
    @Mapping(source = "pedagogicalComment", target = "comments")
    GradeResponseDTO toResponseDto(Grade entity);

    List<GradeResponseDTO> toResponseDto(List<Grade> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "score", target = "grade")
    @Mapping(source = "comments", target = "pedagogicalComment")
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "subject", ignore = true)
    @Mapping(target = "classe", ignore = true)
    @Mapping(target = "sequence", ignore = true)
    @Mapping(target = "trimestre", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(GradeCreateRequestDTO dto, @MappingTarget Grade entity);

    default String buildStudentName(Grade grade) {
        if (grade == null || grade.getStudent() == null) {
            return "";
        }
        String lastName = grade.getStudent().getLastNameStudent() != null ? grade.getStudent().getLastNameStudent() : "";
        String firstName = grade.getStudent().getFirstNameStudent() != null ? grade.getStudent().getFirstNameStudent() : "";
        return (lastName + " " + firstName).trim();
    }

    default String buildSubjectName(Grade grade) {
        if (grade == null || grade.getSubject() == null || grade.getSubject().getNameSubject() == null) {
            return "";
        }
        return grade.getSubject().getNameSubject();
    }

    default String buildClasseName(Grade grade) {
        if (grade == null || grade.getClasse() == null || grade.getClasse().getNameClasse() == null) {
            return "";
        }
        return grade.getClasse().getNameClasse();
    }
}
