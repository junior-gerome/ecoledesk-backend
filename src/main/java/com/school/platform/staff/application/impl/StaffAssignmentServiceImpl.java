package com.school.platform.staff.application.impl;

import com.school.platform.shared.domain.exception.shared.NotFoundException;
import com.school.platform.staff.application.dto.StaffAssignmentCreateRequest;
import com.school.platform.staff.application.dto.StaffAssignmentFullDTO;
import com.school.platform.staff.application.dto.StaffAssignmentMediumDTO;
import com.school.platform.staff.application.interfaces.StaffAssignmentService;
import com.school.platform.staff.application.mapper.StaffAssignmentMapper;
import com.school.platform.staff.domain.model.StaffAssignment;
import com.school.platform.staff.domain.model.StaffMember;
import com.school.platform.staff.domain.model.StaffPosition;
import com.school.platform.staff.infrastructure.persistence.StaffAssignmentRepository;
import com.school.platform.staff.infrastructure.persistence.StaffMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffAssignmentServiceImpl implements StaffAssignmentService {

    private final StaffAssignmentRepository staffAssignmentRepository;
    private final StaffMemberRepository staffMemberRepository;
    private final StaffAssignmentMapper staffAssignmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<StaffAssignmentMediumDTO> getByStaffMember(Long staffMemberId) {
        return staffAssignmentMapper.toMediumDTOList(
                staffAssignmentRepository.findByStaffMemberId(staffMemberId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAssignmentMediumDTO> getActiveByStaffMember(Long staffMemberId) {
        return staffAssignmentMapper.toMediumDTOList(
                staffAssignmentRepository.findActiveByStaffMemberId(staffMemberId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffAssignmentMediumDTO> getByPosition(StaffPosition position) {
        return staffAssignmentMapper.toMediumDTOList(
                staffAssignmentRepository.findByPositionType(position)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StaffAssignmentFullDTO getById(Long id) {
        StaffAssignment assignment = staffAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StaffAssignment", "id", id));
        return staffAssignmentMapper.toFullDTO(assignment);
    }

    @Override
    @Transactional
    public StaffAssignmentFullDTO create(StaffAssignmentCreateRequest request) {
        StaffMember member = staffMemberRepository.findByIdWithPerson(request.getStaffMemberId())
                .orElseThrow(() -> new NotFoundException("StaffMember", "id", request.getStaffMemberId()));

        StaffAssignment assignment = new StaffAssignment();
        assignment.setStaffMember(member);
        assignment.setPosition(request.getPosition());
        assignment.setStartDate(request.getStartDate());
        assignment.setEndDate(request.getEndDate());

        return staffAssignmentMapper.toFullDTO(staffAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public StaffAssignmentFullDTO closeAssignment(Long id) {
        StaffAssignment assignment = staffAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StaffAssignment", "id", id));
        assignment.setEndDate(LocalDate.now());
        return staffAssignmentMapper.toFullDTO(staffAssignmentRepository.save(assignment));
    }
}
