package com.school.platform.identityaccess.application.dto.useraccount;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountMediumDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String roleCode;
    private String roleLabel;
    private String status;
    private Boolean enabled;
}
