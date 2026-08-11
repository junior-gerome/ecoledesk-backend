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
        return staffAssignmentRepository.findByPosition(StaffPosition.TEACHER).stream()
                .filter(this::isActiveToday)
                .map(StaffAssignment::getStaffMember)
                .filter(staff -> Boolean.TRUE.equals(staff.getActive()))
                .distinct()
                .sorted(Comparator.comparing(staff -> staff.getPerson().getLastName(), String.CASE_INSENSITIVE_ORDER))
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
        return !assignment.getStartDate().isAfter(date)
                && (assignment.getEndDate() == null || !assignment.getEndDate().isBefore(date));
    }
}
