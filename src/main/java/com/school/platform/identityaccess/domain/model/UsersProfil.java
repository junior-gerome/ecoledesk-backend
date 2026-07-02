package com.school.platform.identityaccess.domain.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.school.platform.identityaccess.domain.model.RoleType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Entity
@Table(name = "profil_utilisateur")
public class UsersProfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "L'email doit etre valide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(name = "email_user", nullable = false, unique = true, length = 100)
    private String emailUser;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Le prenom est obligatoire")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_user", nullable = false)
    private RoleType roleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Role role;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Users user;

    @Column(name = "reference_id")
    private Long referenceId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEffectiveRoleCode() {
        if (role != null && Boolean.TRUE.equals(role.getActive()) && hasText(role.getCode())) {
            return role.getCode();
        }
        return roleType == null ? null : roleType.name();
    }

    public String getEffectiveRoleLabel() {
        if (role != null && Boolean.TRUE.equals(role.getActive()) && hasText(role.getLabel())) {
            return role.getLabel();
        }
        return roleType == null ? null : roleType.name();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}