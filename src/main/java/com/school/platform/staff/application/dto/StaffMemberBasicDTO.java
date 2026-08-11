package com.school.platform.staff.application.dto;

import com.school.platform.enrollment.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberBasicDTO {
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String photoUrl;
    private Gender gender;
    private Boolean active;
}
