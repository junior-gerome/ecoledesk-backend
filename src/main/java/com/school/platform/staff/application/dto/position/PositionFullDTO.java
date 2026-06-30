package com.school.platform.staff.application.dto.position;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionFullDTO {
    private Long id;
    private String code;
    private String label;
    private String description;
    private Integer employeeCount;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
