package com.school.platform.staff.application.dto.position;

import com.school.platform.staff.domain.model.StaffPosition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffPositionDTO {
    private StaffPosition code;
    private String label;

    public static StaffPositionDTO of(StaffPosition position) {
        return new StaffPositionDTO(position, position.name());
    }
}
