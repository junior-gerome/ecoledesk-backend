package com.school.platform.staff.application.dto;

import com.school.platform.staff.domain.model.StaffPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignmentFullDTO {
    private Long id;
    private StaffMemberBasicDTO staffMember;
    private StaffPosition position;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
