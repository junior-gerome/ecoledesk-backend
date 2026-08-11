package com.school.platform.staff.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.school.platform.staff.domain.model.StaffAssignment;
import com.school.platform.staff.domain.model.StaffPosition;

@Repository
public interface StaffAssignmentRepository extends JpaRepository<StaffAssignment, Long> {

    List<StaffAssignment> findByStaffMemberId(Long staffMemberId);

    @Query("SELECT a FROM StaffAssignment a JOIN FETCH a.staffMember sm JOIN FETCH sm.person WHERE a.staffMember.id = :staffMemberId AND a.endDate IS NULL")
    List<StaffAssignment> findActiveByStaffMemberId(@Param("staffMemberId") Long staffMemberId);

    @Query("SELECT a FROM StaffAssignment a JOIN FETCH a.staffMember sm JOIN FETCH sm.person WHERE a.position = :position")
    List<StaffAssignment> findByPositionType(@Param("position") StaffPosition position);

    @Query("SELECT a FROM StaffAssignment a JOIN FETCH a.staffMember sm JOIN FETCH sm.person WHERE a.position = :position AND a.endDate IS NULL")
    List<StaffAssignment> findByPosition(@Param("position") StaffPosition position);
}
