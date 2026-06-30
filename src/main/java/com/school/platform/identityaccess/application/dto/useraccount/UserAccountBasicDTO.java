package com.school.platform.identityaccess.application.dto.useraccount;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAccountBasicDTO {
    private Long id;
    private PersonBasicDTO person;
    private String username;
    private Boolean enabled;
}
