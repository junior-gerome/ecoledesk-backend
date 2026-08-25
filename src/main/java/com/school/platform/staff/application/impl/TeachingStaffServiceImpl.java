package com.school.platform.staff.application.impl;

import com.school.platform.shared.domain.exception.ResourceNotFoundException;
import com.school.platform.staff.application.interfaces.ITeachingStaffService;
import com.school.platform.staff.domain.model.StaffAssignment;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.staff.domain.model.StaffPosition;
import com.school.platform.staff.infrastructure.persistence.StaffAssignmentRepository;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeachingStaffServiceImpl implements ITeachingStaffService {

    private final StaffMemberRepository staffMemberRepository;
    private final StaffAssignmentRepository staffAssignmentRepository;

    @Override
    @Transactional(readOnly = true)
    public StaffMember getTeachingStaffMember(Long staffMemberId) {
        StaffMember staffMember = staffMemberRepository.findByIdWithPerson(staffMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("StaffMember", "id", staffMemberId));
        if (!isTeacherOn(staffMemberId, LocalDate.now())) {
            throw new ResourceNotFoundException("Enseignant", "staffMemberId", staffMemberId);
        }
        return staffMember;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffMember> getTeachingStaffMembers() {
        // findByPositionType charge tous les assignments TEACHER (avec ou sans endDate)
        // puis isActiveToday filtre ceux dont la période couvre aujourd'hui.
        List<StaffMember> activeTeachers = staffAssignmentRepository.findByPositionType(StaffPosition.TEACHER).stream()
                .filter(this::isActiveToday)
                .map(StaffAssignment::getStaffMember)
                .filter(staff -> staff != null && Boolean.TRUE.equals(staff.getActive()))
                .filter(staff -> staff.getPerson() != null)
                .distinct()
                .sorted(Comparator.comparing(
                        staff -> staff.getPerson().getLastName() != null
                                ? staff.getPerson().getLastName()
                                : "",
                        String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (!activeTeachers.isEmpty()) {
            return activeTeachers;
        }

        // Fallback: charge tous les membres ayant une affectation TEACHER sans filtre de date
        return staffAssignmentRepository.findByPositionType(StaffPosition.TEACHER).stream()
                .map(StaffAssignment::getStaffMember)
                .filter(staff -> staff != null && Boolean.TRUE.equals(staff.getActive()))
                .filter(staff -> staff.getPerson() != null)
                .distinct()
                .sorted(Comparator.comparing(
                        staff -> staff.getPerson().getLastName() != null
                                ? staff.getPerson().getLastName()
                                : "",
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean isTeacherOn(Long staffMemberId, LocalDate date) {
        return staffAssignmentRepository.findByStaffMemberId(staffMemberId).stream()
                .anyMatch(assignment -> assignment.getPosition() == StaffPosition.TEACHER
                        && isActiveOn(assignment, date));
    }

    private boolean isActiveToday(StaffAssignment assignment) {
        return isActiveOn(assignment, LocalDate.now());
    }

    private boolean isActiveOn(StaffAssignment assignment, LocalDate date) {
        if (assignment == null) {
            return false;
        }
        boolean startOk = (assignment.getStartDate() == null || !assignment.getStartDate().isAfter(date));
        boolean endOk = (assignment.getEndDate() == null || !assignment.getEndDate().isBefore(date));
        return startOk && endOk;
    }
}
