package com.school.platform.enrollment.domain.policy;

import com.school.platform.academic.domain.model.ClasseRoom;
import com.school.platform.enrollment.domain.enrollment.Enrollment;
import com.school.platform.enrollment.domain.enrollment.EnrollmentStatus;
import com.school.platform.enrollment.infrastructure.persistence.EnrollmentRepository;
import com.school.platform.shared.domain.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdmissionPolicy {

    private final EnrollmentRepository enrollmentRepository;

    public void assertConfirmationAllowed(Enrollment enrollment, ClasseRoom lockedClassroom) {
        if (!enrollment.getAcademicYear().isStatutCode()) {
            throw new BadRequestException("The academic year is not open");
        }
        if (Boolean.FALSE.equals(lockedClassroom.getActif())) {
            throw new BadRequestException("The classroom is not active");
        }
        if (lockedClassroom.getAcademicYear() != null
                && !lockedClassroom.getAcademicYear().getId().equals(enrollment.getAcademicYear().getId())) {
            throw new BadRequestException("The classroom does not belong to the enrollment academic year");
        }
        String requestedLevel = enrollment.getPreEnrollment().getRequestedLevel();
        if (requestedLevel != null && !requestedLevel.isBlank()
                && !requestedLevel.trim().equalsIgnoreCase(lockedClassroom.getLevel())) {
            throw new BadRequestException("The classroom level does not match the requested level");
        }
        Integer capacity = lockedClassroom.getCapacity();
        if (capacity != null && capacity > 0) {
            long confirmed = enrollmentRepository.countByClassroomIdAndAcademicYearIdAndStatus(
                    lockedClassroom.getId(), enrollment.getAcademicYear().getId(), EnrollmentStatus.CONFIRMED);
            if (confirmed >= capacity) {
                throw new BadRequestException("Class capacity has been reached");
            }
        }
    }
}