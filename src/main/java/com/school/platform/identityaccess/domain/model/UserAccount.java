package com.school.platform.identityaccess.domain.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserAccount extends BaseEntity implements UserDetails {

    @EqualsAndHashCode.Include
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    @EqualsAndHashCode.Include
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50)
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "reset_token", length = 64)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @Override
    public String getPassword() { return passwordHash; }

    public void setPassword(String passwordHash) { this.passwordHash = passwordHash; }

    public String getResetToken() { return resetTokenHash; }

    public void setResetToken(String resetTokenHash) { this.resetTokenHash = resetTokenHash; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> authorityCodes = new HashSet<>();
        for (Role role : roles) {
            if (role == null || Boolean.FALSE.equals(role.getActive())) continue;
            if (role.getCode() != null && !role.getCode().isBlank()) {
                String code = role.getCode().trim().toUpperCase().replace('-', '_').replace(' ', '_');
                authorityCodes.add(code.startsWith("ROLE_") ? code : "ROLE_" + code);
            }
            role.getPermissions().stream()
                    .filter(permission -> !Boolean.FALSE.equals(permission.getActive()))
                    .map(Permission::getCode)
                    .filter(code -> code != null && !code.isBlank())
                    .map(String::trim)
                    .forEach(authorityCodes::add);
        }
        return authorityCodes.stream().map(SimpleGrantedAuthority::new).toList();
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
        return Boolean.TRUE.equals(enabled);
    }
}
