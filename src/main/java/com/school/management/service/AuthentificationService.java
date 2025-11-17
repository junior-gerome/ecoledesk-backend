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

@Transactional
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    // public boolean usernameExists(String username) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'usernameExists'");
    // }
}
