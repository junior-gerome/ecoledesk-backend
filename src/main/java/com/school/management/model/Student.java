package com.school.management.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.school.management.enums.Gender;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String lastNameStudent ;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String firstNameStudent ;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "registration_Date", nullable = true)
    private LocalDateTime registrationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;
    

    @Column(name = "active")
    private Boolean active = true;

    @Size(max = 100, message = "Le nom de la classe ne doit pas dépasser 100 caractères")
    @Column(name = "ecole_precedente", length = 100) 
    private String ecolePrecedente;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // @OneToMany(mappedBy = "student")
    // private List<Absence> absences; 

} 