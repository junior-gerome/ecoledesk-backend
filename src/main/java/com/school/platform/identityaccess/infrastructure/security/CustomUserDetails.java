package com.school.platform.identityaccess.infrastructure.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



public class CustomUserDetails implements UserDetails {
    private String email;
    private String password;
    private List<GrantedAuthority> authorities;
    private boolean active;

    // public CustomUserDetails(User users) {
    //     this.email = users.getEmailUser();
    //     this.password = users.getPassword();
    //     this.active = users.getActif() != null ? users.getActif() : true;
    //     this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + users.getTypePersonne().name()));
    // }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
