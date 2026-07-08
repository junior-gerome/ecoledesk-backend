package com.school.attendance.adapter.out.persistence;

import com.school.attendance.domain.model.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "attendance_records")
class AttendanceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    Long studentId;
    @Column(nullable = false)
    String studentName;
    @Column(nullable = false)
    Long classId;
    @Column(nullable = false)
    String className;
    @Column(name = "attendance_date", nullable = false)
    LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AttendanceStatus status;
    @Column(nullable = false)
    BigDecimal hours;
    @Column(nullable = false)
    boolean justified;
    String justificationNote;
    @UpdateTimestamp
    LocalDateTime updatedAt;
}
