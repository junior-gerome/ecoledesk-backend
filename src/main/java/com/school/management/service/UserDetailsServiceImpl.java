package com.school.management.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.school.management.repository.UsersRepository;
import com.school.management.model.Users;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsersRepository userRepository;

    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            String roles= "ROLE_" + user.getProfils().get(0).getRoleType().name().toUpperCase();
        // Format correct : "ADMIN" au lieu de "ROLE_ADMIN"
        return User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            // .roles(user.getRoleLibelle().toUpperCase().replace("ROLE_", "")) // Nettoyage du préfixe
            .authorities(new SimpleGrantedAuthority(roles))
            .build();
    }
}