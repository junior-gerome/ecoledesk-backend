package com.school.management.dto.auth;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class LoginResponse {
    private String token;
    private Long userId;
    private String NameUser;
    private String Email;
    // private String role;
   @Builder.Default
    private Map<String, Object> userProfile = new HashMap<>();
    // public static Object builder() {
    //   // TODO Auto-generated method stub
    //   throw new UnsupportedOperationException("Unimplemented method 'builder'");
    // }
}


// package com.school.management.dto.auth;
// import lombok.Builder;
// import lombok.Data;

// @Data
// @Builder
// public class LoginResponse {
//     private String token;
//     private Long userId;
//     private String nameUser;
//     private String role;
// }
