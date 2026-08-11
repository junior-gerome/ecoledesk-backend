package com.school.platform.staff.application.dto;

import com.school.platform.enrollment.domain.model.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberFullDTO {
    private Long id;
    private String employeeNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Gender gender;
    private LocalDate birthDate;
    private LocalDate employmentDate;
    private String speciality;
    private String level;
    private String address;
    private String city;
    private String country;
    private String photoUrl;
    private String cniNumber;
    private String cniPhotoUrl;
    private Boolean active;
    private LocalDateTime creationDate;
    private LocalDateTime updateDate;
    private List<StaffAssignmentBasicDTO> assignments;
}
