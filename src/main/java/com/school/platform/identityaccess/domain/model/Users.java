package com.school.platform.identityaccess.domain.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "profils")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class Users implements UserDetails {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String passwordHash;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UsersProfil> profils;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "reset_token", length = 64)
    private String resetTokenHash;

    @Column(name = "reset_token_expires_at")
    private LocalDateTime resetTokenExpiresAt;

    @Column(nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String getPassword() {
        return passwordHash;
    }

    public void setPassword(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getResetToken() {
        return resetTokenHash;
    }

    public void setResetToken(String resetTokenHash) {
        this.resetTokenHash = resetTokenHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (profils == null || profils.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> authorities = new LinkedHashSet<>();
        for (UsersProfil profil : profils) {
            addProfileAuthorities(authorities, profil);
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    private void addProfileAuthorities(Set<String> authorities, UsersProfil profil) {
        Role dynamicRole = profil.getRole();
        if (dynamicRole != null && !Boolean.FALSE.equals(dynamicRole.getActive())) {
            addRoleAuthority(authorities, dynamicRole.getCode());
            if (dynamicRole.getPermissions() != null) {
                dynamicRole.getPermissions().stream()
                        .filter(permission -> !Boolean.FALSE.equals(permission.getActive()))
                        .map(Permission::getCode)
                        .filter(this::hasText)
                        .map(String::trim)
                        .forEach(authorities::add);
            }
            return;
        }

        if (profil.getRoleType() != null) {
            addRoleAuthority(authorities, profil.getRoleType().name());
        }
    }

    private void addRoleAuthority(Set<String> authorities, String roleCode) {
        if (!hasText(roleCode)) {
            return;
        }
        String normalized = roleCode.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        authorities.add(normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
        return actif != null && actif;
    }
}
