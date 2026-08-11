package com.school.platform.enrollment.application.dto.guardian;

import com.school.platform.identityaccess.application.dto.person.PersonMediumDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuardianMediumDTO {
    private Long id;
    private PersonMediumDTO person;
    private String occupation;
    private Integer childrenCount;
}
