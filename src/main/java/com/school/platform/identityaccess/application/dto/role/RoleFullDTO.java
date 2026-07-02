package com.school.platform.identityaccess.application.dto.role;

import com.school.platform.identityaccess.application.dto.permission.PermissionBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleFullDTO {
    private Long id;
    private String code;
    private String label;
    private String description;
    private Set<PermissionBasicDTO> permissions;
    private Integer usersCount;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
