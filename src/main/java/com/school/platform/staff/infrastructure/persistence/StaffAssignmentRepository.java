package com.school.platform.staff.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.school.platform.staff.domain.model.StaffAssignment;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {
    List<StaffAssignment> findByStaffMemberId(Long staffMemberId);

    @Query("SELECT assignment FROM StaffAssignment assignment WHERE assignment.staffMember.id = :staffMemberId AND assignment.endDate IS NULL")
    List<StaffAssignment> findActiveAssignmentsByStaffMemberId(Long staffMemberId);
}
