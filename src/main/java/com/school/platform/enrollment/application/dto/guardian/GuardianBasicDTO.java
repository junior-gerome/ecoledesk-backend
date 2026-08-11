package com.school.platform.enrollment.application.dto.guardian;

import com.school.platform.identityaccess.application.dto.person.PersonBasicDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardianBasicDTO {
    private Long id;
    private PersonBasicDTO person;
    private String occupation;
}
