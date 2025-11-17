package com.school.management.model;


import java.time.LocalDateTime;

import com.school.management.enums.TypeParent;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parents")
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nom-parent", nullable = false)
    private String lastNameParent; 

    @Column(name="prenom-parent", nullable = false)
    private String firstNameParent ;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column( unique = true, length = 100)
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    @Column( unique = true, length =20)
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String phoneNumber;

     @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    @Column(name ="address", length = 200)
    private String address;
    
    @Column(name = "profession-parent", nullable = false, length =100)
    private String professionParent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_parent", nullable = false)
    private TypeParent typeParent;

     @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
