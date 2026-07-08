package com.school.platform.identityaccess.application.dto.auth;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long userId;

    @JsonProperty("NameUser")
    private String nameUser;

    @JsonProperty("Email")
    private String email;

    @Builder.Default
    private Map<String, Object> userProfile = new HashMap<>();
}
