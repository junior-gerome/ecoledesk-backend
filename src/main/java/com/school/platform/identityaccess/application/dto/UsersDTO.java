package com.school.platform.identityaccess.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.school.platform.identityaccess.domain.model.RoleType;
import com.school.platform.identityaccess.domain.model.UsersProfil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UsersDTO {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private RoleType roleType;
    private Long roleId;
    private String roleCode;
    private String roleLabel;
    private String status;
    private List<String> permissions;
    private List<UsersProfil> profils;
    private Boolean actif;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public UsersDTO() {
        // Default constructor
    }
}