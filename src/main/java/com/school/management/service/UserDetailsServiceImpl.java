package com.school.management.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ Correct import

import com.school.management.repository.UsersRepository;
import com.school.management.model.Users;
//import com.school.management.model.UsersProfil;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Ici user.getProfils() est déjà chargé (grâce à @EntityGraph ou fetch join)
        List<GrantedAuthority> authorities = user.getAuthorities().stream().collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled(),
            true, true, true,
            authorities
        );
    }

    // @Override
    // @Transactional(readOnly = true) // ✅ Now works because we use Spring's annotation
    // public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //     Users user = userRepository.findByUsernameWithProfils(username)
    //             .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));

    //     // Récupérer tous les rôles des profils et les transformer en authorities
    //     List<GrantedAuthority> authorities = user.getProfils().stream()
    //             .map(UsersProfil::getRoleType) // Enum RoleType
    //             .map(roleType -> new SimpleGrantedAuthority("ROLE_" + roleType.name())) // Ajoute le préfixe ROLE_
    //             .collect(Collectors.toList());

    //     return new org.springframework.security.core.userdetails.User(
    //             user.getUsername(),
    //             user.getPassword(),
    //             user.isEnabled(),  // basé sur 'actif'
    //             true,  // accountNonExpired
    //             true,  // credentialsNonExpired
    //             true,  // accountNonLocked
    //             authorities
    //     );
    // }
}
