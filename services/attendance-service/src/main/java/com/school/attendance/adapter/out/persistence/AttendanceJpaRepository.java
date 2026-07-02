package com.school.attendance.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

interface AttendanceJpaRepository extends JpaRepository<AttendanceJpaEntity, Long>, JpaSpecificationExecutor<AttendanceJpaEntity> {
}
