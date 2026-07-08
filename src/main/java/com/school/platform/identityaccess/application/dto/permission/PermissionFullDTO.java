package com.school.platform.identityaccess.application.dto.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionFullDTO {
    private Long id;
    private String code;
    private String description;
    private String resource;
    private String action;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
