package com.school.management.model;


import com.school.management.enums.TypeParent;

import jakarta.persistence.*;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "parents")
public class Parent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nom-parent", nullable = false)
    private String LastNameParent; 

    @Column(name="prenom-parent", nullable = false)
    private String FirstNameParent ;

    //  @Column(name="nom-mere", nullable = false)
    // private String LastNameMother ;

    // @Column(name="prenom-mere", nullable = false)
    // private String FirstNameMother ;

    @Size(max = 200, message = "L'adresse ne doit pas dépasser 200 caractères")
    @Column(length = 200)
    private String Adresse;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(unique = true, length = 100)
    private String Email;


    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide")
    @Column(name = "phone-number", unique = true, length = 15)
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    private String Phonenumber;

    

    @Column(name = "profession-parent", nullable = false, length =100)
    private String professionParent;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_parent", nullable = false)
    private TypeParent typeParent;

    // @Column(name = "profession-mere", nullable = false, length =100)
    // private String professionMere;
}
