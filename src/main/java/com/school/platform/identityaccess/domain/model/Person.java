package com.school.platform.identityaccess.domain.model;

import com.school.platform.shared.domain.BaseEntity;

import com.school.platform.enrollment.domain.model.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Person extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Email(message = "L'email doit être valide")
    @Column(name = "email", unique = true, length = 150)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;
}
