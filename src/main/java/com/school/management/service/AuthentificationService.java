// // src/main/java/com/school/management/service/AuthenticationService.java
// package com.school.management.service;

// import com.school.management.dto.auth.LoginRequest;
// import com.school.management.dto.auth.LoginResponse;
// import com.school.management.model.Users;
// import com.school.management.repository.UsersRepository;
// import com.school.management.security.JwtTokenProvider;

// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class AuthenticationService {

//     private final AuthenticationManager authenticationManager;
//     private final JwtTokenProvider tokenProvider;
//     private final UsersRepository userRepository;
//     private final UsersService userService;

//     @Transactional
//     public LoginResponse login(LoginRequest loginRequest) {
//         // 1) Authentifier via Spring Security
//         Authentication authentication = authenticationManager.authenticate(
//             new UsernamePasswordAuthenticationToken(
//                 loginRequest.getEmail(),
//                 loginRequest.getPassword()
//             )
//         );
//         SecurityContextHolder.getContext().setAuthentication(authentication);

//         // 2) Charger l’utilisateur
//         Users user = userRepository.findByUsername(loginRequest.getEmail())
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

//         // 3) Générer le JWT
//         String jwt = tokenProvider.generateToken(authentication);

//         // 4) Mettre à jour la date de dernier login
//         userService.updateLastLogin(user.getId());

//         // // 5) Construire la réponse
//         //  LoginResponse response = new LoginResponse();
//         //                 response.setToken(jwt);
//         //                 response.setUserId(user.getId());
//         //                 response.setNameUser(user.getUserProfil().getFirstName() + " " + user.getUserProfil().getLastName());
//         //                 response.setEmail(user.getUserProfil().getEmailUser());
//         //                 response.setRole(user.getRole().getLibelle());
//         //                 response.setUsersProfil(user.getUserProfil());
//         //                 return response;

//         return LoginResponse.builder()
//             .token(jwt)
//             .userId(user.getId())
//             .nameUser(user.getUsername())
//             .role(user.getRole().getLibelle())
//             .build();
//     }

//     @Transactional
//     public void logout() {
//         SecurityContextHolder.clearContext();
//     }
// }


// src/main/java/com/school/management/service/AuthenticationService.java
package com.school.management.service;

import com.school.management.dto.auth.LoginRequest;
import com.school.management.dto.auth.LoginResponse;
import com.school.management.dto.auth.RegisterRequest;
import com.school.management.model.Users;
import com.school.management.model.UsersProfil;
import com.school.management.repository.UsersRepository;
import com.school.management.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthentificationService {
    
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    @Transactional
    public LoginResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    
        Users user = usersRepository.findByUsername(req.getEmail())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        String token = jwtService.generateToken(user);
    
        // Construire la réponse
        UsersProfil profil = user.getProfils().get(0);
    
    return LoginResponse.builder()
        .token(token)
        .userId(user.getId())
        .NameUser(profil.getFirstName() + " " + profil.getLastName())
        .Email(user.getUsername())
        .userProfile(Map.of(
            "id", profil.getId(),
            "firstName", profil.getFirstName(),
            "lastName", profil.getLastName(),
            "roleType", profil.getRoleType().name()
        ))
        .build();
    }
    

    // @Transactional
    // public LoginResponse login(LoginRequest req) {
    //     Authentication auth = authenticationManager.authenticate(
    //         new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
    //     );
    //     SecurityContextHolder.getContext().setAuthentication(auth);

    //     Users user = usersRepository.findByUsername(req.getEmail())
    //         .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

    //     String token = jwtService.generateToken(user);
    //     // mettez ici à jour lastLogin si besoin...

    //     // LoginResponse resp = new LoginResponse();
    //     // resp.setToken(token);
    //     // resp.setUserId(user.getId());
    //     // resp.setNameUser(user.getUserProfil().getFirstName() + " " + user.getUserProfil().getLastName());
    //     // resp.setEmail(user.getUserProfil().getEmailUser());
    //     // resp.setRole(user.getRole().getLibelle());
    //     // resp.setUsersProfil(user.getUserProfil());
    //     // return resp;

    //     return LoginResponse.builder()
    //     .token(token)
    //     .userId(user.getId())
    //     .Email(user.getUsername())
    //     .NameUser(user.getProfils().get(0).getFirstName() + " " + user.getProfils().get(0).getLastName())
    //     .usersProfil(user.getProfils().get(0))
    //     // .role(user.getProfils().get(0).getRoleType().name)
    //     .build();
    // }

    @Transactional
public void register(RegisterRequest req) {
    if (usersRepository.existsByUsername(req.getEmail())) {
        throw new IllegalArgumentException("Email déjà utilisé");
    }
    Users user = new Users();
    user.setUsername(req.getEmail());
    user.setPassword(passwordEncoder.encode(req.getPassword()));
    user.setActif(true);

    UsersProfil profil = new UsersProfil();
    profil.setEmailUser(req.getEmail());
    profil.setFirstName(req.getFirstName());
    profil.setLastName(req.getLastName());
    profil.setRoleType(req.getRoleType());
    profil.setReferenceId(req.getReferenceId());
    profil.setUser(user); // Liaison bidirectionnelle

    user.setProfils(List.of(profil)); // Cascade ALL permet de sauvegarder le profil automatiquement

    usersRepository.save(user);
}


    // @Transactional
    // public void register(RegisterRequest req) {
    //     Users user = new Users();
    //     user.setUsername(req.getEmail());
    //     user.setPassword(passwordEncoder.encode(req.getPassword()));
    //     user.setActif(true);
    
    //     UsersProfil profil = new UsersProfil();
    //     profil.setEmailUser(req.getEmail());
    //     profil.setFirstName(req.getFirstName());
    //     profil.setLastName(req.getLastName());
    //     profil.setRoleType(req.getRoleType());
    //     profil.setReferenceId(req.getReferenceId());
    //     profil.setUser(user); // Liaison
    
    //     user.setProfils(List.of(profil)); // IMPORTANT pour le mappedBy
    
    //     usersRepository.save(user); // Grâce au cascade ALL, le profil est enregistré
    // }
    
    // public void  register(RegisterRequest req) {
    //     if (usersRepository.existsByUsername(req.getEmail())) {
    //         throw new IllegalArgumentException("Email déjà utilisé");
    //     }

    //     // Role role = roleRepository.findById(req.getRoleId())
    //     //     .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

    //     Users user = new Users();
    //     user.setUsername(req.getEmail());
    //     user.setPassword(passwordEncoder.encode(req.getPassword()));
    //     // user.setRole(role);
    //     user.setActif(true);
    //     usersRepository.save(user);

    //     UsersProfil profil = new UsersProfil();
    //     profil.setEmailUser(req.getEmail());
    //     profil.setFirstName(req.getFirstName());
    //     profil.setLastName(req.getLastName());
    //     profil.setRoleType(req.getRoleType());
    //     profil.setReferenceId(req.getReferenceId());
    //     profil.setUser(user);

    //     profilRepository.save(profil);

    //     // String token = jwtService.generateToken(user);
    //     // LoginResponse resp = new LoginResponse();
    //     // resp.setToken(token);
    //     // resp.setUserId(user.getId());
    //     // resp.setNameUser(req.getFirstName() + " " + req.getLastName());
    //     // resp.setEmail(req.getEmail());
    //     // resp.setRole(role.getLibelle());
    //     // resp.setUsersProfil(profil);
    //     // return resp;
    // }

    @Transactional
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    // public boolean usernameExists(String username) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'usernameExists'");
    // }
}
