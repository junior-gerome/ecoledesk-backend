package com.school.platform.staff.application.dto;

import com.school.platform.enrollment.domain.model.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberCreateRequest {

    @NotBlank(message = "Le numéro employé est obligatoire")
    private String employeeNumber;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    private String email;
    private String phone;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    @NotNull(message = "La date d'embauche est obligatoire")
    private LocalDate employmentDate;

    private LocalDate birthDate;
    private String speciality;
    private String level;
    private String address;
    private String city;
    private String country;
    private String photoUrl;
    private String cniNumber;
    private String cniPhotoUrl;
}
