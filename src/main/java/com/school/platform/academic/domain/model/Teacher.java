package com.school.platform.academic.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.school.platform.enrollment.domain.model.Gender;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "enseignant")
public class Teacher {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false, length = 100)
    private String lastnameTeacher;

    @NotBlank(message = "Le prénom est obligatoire")
    @Column(nullable = false, length = 100)
    private String firstnameTeacher;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @NotNull(message = "Le genre est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, columnDefinition = "ENUM('MASCULIN', 'FEMININ')")
    private Gender gender;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    @Column(nullable = false, length = 20, unique = true)
    private String phoneNumber;

    @Column(length = 100)
    private String speciality;

    @Column(length = 50)
    private String niveau;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "adress")
    private String adress;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "cni_number", length = 50)
    private String cniNumber;

    @Column(name = "cni_photo_url", length = 500)
    private String cniPhotoUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
