package com.school.platform.staff.application.dto;

import com.school.platform.staff.domain.model.StaffPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffAssignmentBasicDTO {
    private Long id;
    private StaffPosition position;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
