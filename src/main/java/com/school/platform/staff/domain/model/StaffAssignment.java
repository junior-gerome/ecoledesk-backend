package com.school.platform.staff.domain.model;

import java.time.LocalDate;

import com.school.platform.identityaccess.domain.model.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff_assignments")
@Getter
@Setter
@NoArgsConstructor
public class StaffAssignment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "staff_member_id", nullable = false)
    @NotNull
    private StaffMember staffMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "professional_function", nullable = false, length = 50)
    @NotNull
    private StaffPosition position;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;
}
