package com.school.platform.identityaccess.infrastructure.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.school.platform.identityaccess.domain.model.UserAccount;
import com.school.platform.identityaccess.infrastructure.persistence.UserAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = user.getAuthorities().stream()
                .map(GrantedAuthority.class::cast)
                .collect(Collectors.toList());

        return new AuthenticatedUserPrincipal(
                user.getId(), user.getUsername(), user.getPassword(), user.isEnabled(), authorities);
    }
}
