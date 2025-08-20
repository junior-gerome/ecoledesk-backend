// package com.school.management.service;

// import com.school.management.dto.UsersDTO;
// import com.school.management.model.Users;
// import com.school.management.model.Role;
// import com.school.management.repository.UsersRepository;
// import com.school.management.repository.RoleRepository;
// import lombok.RequiredArgsConstructor;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.UUID;
// import java.util.stream.Collectors;

// @Service
// @RequiredArgsConstructor
// public class UsersService {
//     private final UsersRepository userRepository;
//     private final RoleRepository roleRepository;
//     private final PasswordEncoder passwordEncoder;

//     @Transactional(readOnly = true)
//     public List<UsersDTO> getAllUsers() {
//         return userRepository.findAll().stream()
//                 .map(this::convertToDTO)
//                 .collect(Collectors.toList());
//     }

//     public boolean existsByUsername(String username) {
//         return userRepository.existsByUsername(username);
//     }

//     @Transactional(readOnly = true)
//     public UsersDTO getUserById(Long id) {
//         return userRepository.findById(id)
//                 .map(this::convertToDTO)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//     }

//     @Transactional
//     public UsersDTO createUser(UsersDTO dto) {
//         if (userRepository.existsByUsername(dto.getUsername())) {
//             throw new RuntimeException("Username déjà utilisé");
//         }

//         Users users = new Users();
//         updateUserFromDTO(users, dto);
//         users.setPassword(passwordEncoder.encode(dto.getPassword()));
//         users.setActif(true);

//         return convertToDTO(userRepository.save(users));
//     }
    

//     @Transactional
//     public UsersDTO updateUser(Long id, UsersDTO dto) {
//         Users users = userRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

//         if (!users.getUsername().equals(dto.getUsername()) && 
//         userRepository.existsByUsername(dto.getUsername())) {
//             throw new RuntimeException("Usename déjà utilisé");
//         }

//         updateUserFromDTO(users, dto);
//         if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
//             users.setPassword(passwordEncoder.encode(dto.getPassword()));
//         }

//         return convertToDTO(userRepository.save(users));
//     }

//     @Transactional
//     public void deleteUser(Long id) {
//         Users users = userRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//                 users.setActif(false);
//         userRepository.save(users);
//     }

//     @Transactional
//     public void updateLastLogin(Long id) {
//         Users users = userRepository.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
//                 users.setLastLogin(LocalDateTime.now());
//         userRepository.save(users);
//     }

//     @Transactional
//     public String generatePasswordResetToken(String name) {
//         Users users = userRepository.findById(null)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
//         String resetToken = UUID.randomUUID().toString();
//         users.setResetToken(resetToken);
//         userRepository.save(users);
        
//         return resetToken;
//     }

//     @Transactional
//     public void resetPassword(String email, String resetToken, String newPassword) {
//         Users users = userRepository.findById(null)
//                 .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

//         if (!resetToken.equals(users.getResetToken())) {
//             throw new RuntimeException("Token de réinitialisation invalide");
//         }

//         users.setPassword(passwordEncoder.encode(newPassword));
//         users.setResetToken(null);
//         userRepository.save(users);
//     }

//     private void updateUserFromDTO(Users users, UsersDTO dto) {
//         users.setUsername(dto.getUsername());
        
//         Role role = roleRepository.findById(dto.getRoleId())
//                 .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
//                 users.setRole(role);
//     }

//     private UsersDTO convertToDTO(Users users) {
//         UsersDTO dto = new UsersDTO();
//         dto.setId(users.getId());
//         dto.setUsername(users.getUsername());;
//         dto.setRoleId(users.getRole().getId());
//         dto.setRoleLibelle(users.getRole().getLibelle());
//         dto.setActif(users.getActif());
//         dto.setLastLogin(users.getLastLogin());
//         dto.setCreatedAt(users.getCreatedAt());
//         return dto;
//     }

//     public UsersDTO authenticateUserByUsername(String username, String password) {
//         // Recherche de l'utilisateur par son nom d'utilisateur
//         Users user = userRepository.findByUsername(username)
//                 .orElseThrow(() -> new RuntimeException("Nom d'utilisateur ou mot de passe incorrect"));
    
//         // Vérification du mot de passe
//         if (!passwordEncoder.matches(password, user.getPassword())) {
//             throw new RuntimeException("Nom d'utilisateur ou mot de passe incorrect");
//         }
    
//         // Mise à jour du dernier login
//         user.setLastLogin(LocalDateTime.now());
//         userRepository.save(user);
    
//         // Conversion en DTO
//         return convertToDTO(user);
//     }
// }



package com.school.management.service;

import com.school.management.dto.UsersDTO;
import com.school.management.dto.auth.RegisterRequest;
import com.school.management.model.Users;
import com.school.management.model.UsersProfil;
import com.school.management.repository.UsersRepository;
// import com.school.management.repository.RoleRepository;
import com.school.management.repository.UsersProfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository userRepository;
    // private final RoleRepository roleRepository;
    private final UsersProfilRepository profilRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsersDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public UsersDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Méthode utilisée par AuthController pour l'enregistrement
     */
    @Transactional
    public UsersDTO registerUser(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        // Créer l'entité Users
        Users user = new Users();
        user.setUsername(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setActif(true);
        // Affecter le rôle
        // Role role = roleRepository.findById(req.getRoleId())
        //         .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        // user.setRole(role);
        // Sauvegarde initiale pour générer l'ID
        user = userRepository.save(user);

        // Créer le profil associé
        UsersProfil profil = new UsersProfil();
        profil.setEmailUser(req.getEmail());
        profil.setFirstName(req.getFirstName());
        profil.setLastName(req.getLastName());
        profil.setRoleType(req.getRoleType()); // à adapter selon votre logique
        profil.setReferenceId(req.getReferenceId());
        profil.setUser(user);

        profilRepository.save(profil);

        return convertToDTO(user);
    }

    @Transactional
    public UsersDTO updateUser(Long id, UsersDTO dto) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!user.getUsername().equals(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username déjà utilisé");
        }
        // Mise à jour du mot de passe si fourni
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        // Mise à jour du rôle
        // if (!user.getRole().getId().equals(dto.getRoleId())) {
        //     Role role = roleRepository.findById(dto.getRoleId())
        //             .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
        //     user.setRole(role);
        // }
        return convertToDTO(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(false);
        userRepository.save(user);
    }

    @Transactional
    public void updateLastLogin(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public String generatePasswordResetToken(String email) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);
        return resetToken;
    }

    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        if (!resetToken.equals(user.getResetToken())) {
            throw new RuntimeException("Token de réinitialisation invalide");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
    }

    private UsersDTO convertToDTO(Users user) {
        UsersDTO dto = new UsersDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        // dto.setRoleId(user.getRole().getId());
        // dto.setRoleLibelle(user.getRole().getLibelle());
        dto.setActif(user.getActif());
        dto.setLastLogin(user.getLastLogin());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

