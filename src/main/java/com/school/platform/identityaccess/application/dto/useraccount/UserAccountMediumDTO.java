package com.school.platform.identityaccess.application.dto.useraccount;

import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountMediumDTO {
    private Long id;
    private PersonMediumDTO person;
    private String username;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private Integer rolesCount;
}
