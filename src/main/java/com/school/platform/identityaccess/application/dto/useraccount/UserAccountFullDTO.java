package com.school.platform.identityaccess.application.dto.useraccount;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import com.school.platform.identityaccess.application.dto.role.RoleBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountFullDTO {
    private Long id;
    private PersonBasicDTO person;
    private String username;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private Set<RoleBasicDTO> roles;
    private List<String> permissions;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
}
