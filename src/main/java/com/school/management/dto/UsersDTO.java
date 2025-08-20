package com.school.management.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.school.management.model.UsersProfil;

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
    private List<UsersProfil> Profils;
    private Boolean actif;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public UsersDTO() {
        // Default constructor
    }
}
