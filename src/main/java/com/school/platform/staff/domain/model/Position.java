package com.school.platform.staff.domain.model;


import com.school.platform.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "positions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Position extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le code de la position est obligatoire")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "Le libellé de la position est obligatoire")
    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "description", length = 255)
    private String description;
}
