package com.school.platform.enrollment.application.mapper;

import com.school.platform.enrollment.application.dto.preenrollment.*;
import com.school.platform.enrollment.domain.preenrollment.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PreEnrollmentMapper {

    @Mapping(target = "number", source = "number.value")
    @Mapping(target = "academicYearId", source = "academicYear.id")
    PreEnrollmentResponse toResponse(
            PreEnrollment preEnrollment
    );

    default PreEnrollmentDocument toDocument(
            AddPreEnrollmentDocumentRequest request,
            PreEnrollment preEnrollment
    ) {
        if (request == null) {
            return null;
        }

        if (preEnrollment == null) {
            throw new IllegalArgumentException(
                    "Pre-enrollment is required"
            );
        }

        return PreEnrollmentDocument.submitted(
                preEnrollment,
                request.getDocumentType(),
                request.getStorageReference()
        );
    }
}