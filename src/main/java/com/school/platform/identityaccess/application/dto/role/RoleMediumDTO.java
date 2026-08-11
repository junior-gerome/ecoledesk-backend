package com.school.platform.identityaccess.application.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleMediumDTO {
    private Long id;
    private String code;
    private String label;
    private String description;
    private Boolean active;
}
