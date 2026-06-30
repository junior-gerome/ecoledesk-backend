package com.school.platform.enrollment.application.dto;

import com.school.platform.enrollment.domain.model.TypeParent;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ParentDTO {
    private Long id;

    @NotBlank(message = "Le nom du parent est obligatoire")
    @Size(max = 100, message = "Le nom du parent ne doit pas depasser 100 caracteres")
    private String lastNameParent;

    @NotBlank(message = "Le prenom du parent est obligatoire")
    @Size(max = 100, message = "Le prenom du parent ne doit pas depasser 100 caracteres")
    private String firstNameParent;

    @Email(message = "L'email doit etre valide")
    @Size(max = 150, message = "L'email ne doit pas depasser 150 caracteres")
    private String email;

    @NotBlank(message = "Le telephone du parent est obligatoire")
    @Size(max = 30, message = "Le telephone ne doit pas depasser 30 caracteres")
    private String phoneNumber;

    @Size(max = 255, message = "L'adresse ne doit pas depasser 255 caracteres")
    private String address;

    @Size(max = 100, message = "La profession ne doit pas depasser 100 caracteres")
    private String professionParent;

    private String photoUrl;
    private String cniNumber;
    private String cniPhotoUrl;

    @NotNull(message = "Le type de parent est obligatoire")
    private TypeParent typeParent;
}
