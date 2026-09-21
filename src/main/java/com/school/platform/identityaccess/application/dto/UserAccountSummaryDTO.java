package com.school.platform.identityaccess.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.school.platform.identityaccess.application.dto.role.RoleBasicDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class UserAccountSummaryDTO {
    private Long id;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private Long roleId;
    @JsonAlias("roleType")
    private String roleCode;
    private String roleLabel;
    private List<RoleBasicDTO> roles;
    private String status;
    private List<String> permissions;
    private Boolean actif;
    private LocalDateTime lastLogin;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public UserAccountSummaryDTO() { }
}

