package com.school.platform.staff.application.dto.position;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionBasicDTO {
    private Long id;
    private String code;
    private String label;
}
