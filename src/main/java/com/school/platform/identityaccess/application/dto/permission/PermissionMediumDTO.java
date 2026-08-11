package com.school.platform.identityaccess.application.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionMediumDTO {
    private Long id;
    private String code;
    private String description;
    private String resource;
    private String action;
    private Boolean active;
}
