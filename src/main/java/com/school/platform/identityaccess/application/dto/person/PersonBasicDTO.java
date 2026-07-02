package com.school.platform.identityaccess.application.dto.person;

import com.school.platform.enrollment.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonBasicDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Gender gender;
}
