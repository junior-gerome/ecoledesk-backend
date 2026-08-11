package com.school.platform.identityaccess.application.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionBasicDTO {
    private Long id;
    private String code;
    private String description;
}
