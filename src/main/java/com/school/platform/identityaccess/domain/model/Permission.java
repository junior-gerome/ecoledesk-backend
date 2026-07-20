package com.school.platform.identityaccess.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Permission extends BaseEntity {

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le code de la permission est obligatoire")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "resource", length = 50)
    private String resource;

    @Column(name = "action", length = 50)
    private String action;
}
