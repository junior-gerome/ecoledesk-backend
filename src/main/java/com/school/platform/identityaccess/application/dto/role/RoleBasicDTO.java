package com.school.platform.identityaccess.application.dto.role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleBasicDTO {
    private Long id;
    private String code;
    private String label;
}
