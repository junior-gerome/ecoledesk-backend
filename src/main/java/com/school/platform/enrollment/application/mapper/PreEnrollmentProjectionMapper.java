package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentBasicDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentFullDTO;
import com.school.platform.enrollment.application.dto.preenrollment.PreEnrollmentMediumDTO;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollment;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentDocument;
import com.school.platform.enrollment.domain.preenrollment.PreEnrollmentGuardian;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PreEnrollmentProjectionMapper {

    @Mapping(target = "applicantFirstName", source = "applicant.firstName")
    @Mapping(target = "applicantLastName", source = "applicant.lastName")
    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    PreEnrollmentBasicDTO toBasicDTO(PreEnrollment entity);

    @Mapping(target = "applicantFirstName", source = "applicant.firstName")
    @Mapping(target = "applicantLastName", source = "applicant.lastName")
    @Mapping(target = "applicantBirthDate", source = "applicant.birthDate")
    @Mapping(target = "applicantGender", source = "applicant.gender")
    @Mapping(target = "applicantBirthPlace", source = "applicant.birthPlace")
    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.libelleAcademicYear")
    PreEnrollmentMediumDTO toMediumDTO(PreEnrollment entity);

    @Mapping(target = "applicantFirstName", source = "applicant.firstName")
    @Mapping(target = "applicantLastName", source = "applicant.lastName")
    @Mapping(target = "applicantBirthDate", source = "applicant.birthDate")
    @Mapping(target = "applicantGender", source = "applicant.gender")
    @Mapping(target = "applicantBirthPlace", source = "applicant.birthPlace")
    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    @Mapping(target = "academicYearLabel", source = "academicYear.libelleAcademicYear")
    @Mapping(target = "guardians", expression = "java(mapGuardians(entity))")
    @Mapping(target = "documents", expression = "java(mapDocuments(entity))")
    PreEnrollmentFullDTO toFullDTO(PreEnrollment entity);

    List<PreEnrollmentBasicDTO> toBasicDTOList(List<PreEnrollment> entities);

    List<PreEnrollmentMediumDTO> toMediumDTOList(List<PreEnrollment> entities);

    default List<PreEnrollmentFullDTO.PreEnrollmentGuardianDTO> mapGuardians(PreEnrollment entity) {
        if (entity.getGuardians() == null) return List.of();
        return entity.getGuardians().stream()
                .map(g -> new PreEnrollmentFullDTO.PreEnrollmentGuardianDTO(
                        g.getId(), g.getRelationshipType(), g.getFirstName(), g.getLastName(),
                        g.getEmail(), g.getPhoneNumber(), g.getAddress(),
                        g.isPrimaryContact(), g.isFinancialResponsible(), g.isEmergencyContact()))
                .collect(Collectors.toList());
    }

    default List<PreEnrollmentFullDTO.PreEnrollmentDocumentDTO> mapDocuments(PreEnrollment entity) {
        if (entity.getDocuments() == null) return List.of();
        return entity.getDocuments().stream()
                .map(d -> new PreEnrollmentFullDTO.PreEnrollmentDocumentDTO(
                        d.getId(), d.getDocumentType(), d.getStorageReference(),
                        d.getReviewStatus(), d.getRejectionReason(),
                        d.getSubmittedAt(), d.getReviewedAt()))
                .collect(Collectors.toList());
    }
}
