package com.school.platform.identityaccess.application.dto.person;

import com.school.platform.enrollment.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonMediumDTO {
//    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String city;
    private Boolean active;
}
