package com.school.gestionuser.domain.model.identity;

import com.school.gestionuser.domain.model.common.BaseEntity;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entité d'authentification. Implémente UserDetails pour Spring Security.
 *
 * IMPORTANT : User est intentionnellement séparé de Person.
 * User = artefact technique d'authentification
 * Person = entité métier du monde réel
 *
 * Cela permet :
 * - Authentification multi-facteurs sans toucher à Person
 * - Anonymisation RGPD (suppression User sans perte de données Person)
 * - SSO / OAuth2 sans coupling avec les données personnelles
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username", unique = true)
})
public class User extends BaseEntity implements UserDetails {

    @Column(name = "username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;

    /**
     * FK vers Person. Un User est toujours lié à une Person.
     * La FK est côté User (table users.person_id).
     * Relation 1:1 : une Person ne peut avoir qu'un seul User.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false, unique = true)
    private Person person;

    /**
     * Rôles de sécurité avec temporalité.
     * Utilisation d'une entité de jointure (pas @ManyToMany direct)
     * pour stocker validFrom, validTo, assignedBy.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    protected User() {}

    public User(String username, String passwordHash, Person person) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.person = person;
    }

    // === Spring Security UserDetails ===

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
            .filter(UserRole::isActive)
            .flatMap(ur -> {
                Set<GrantedAuthority> authorities = new HashSet<>();
                // Ajoute le rôle lui-même : ROLE_TEACHER
                authorities.add(new SimpleGrantedAuthority(ur.getRole().getCode()));
                // Ajoute chaque permission : students:read, grades:write
                ur.getRole().getRolePermissions().stream()
                    .map(rp -> new SimpleGrantedAuthority(rp.getPermission().getCode()))
                    .forEach(authorities::add);
                return authorities.stream();
            })
            .collect(Collectors.toSet());
    }

    @Override public String getPassword() { return passwordHash; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return accountNonExpired; }
    @Override public boolean isAccountNonLocked() { return accountNonLocked; }
    @Override public boolean isCredentialsNonExpired() { return credentialsNonExpired; }
    @Override public boolean isEnabled() { return enabled; }

    // Domain methods
    public void recordSuccessfulLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.failedLoginAttempts = 0;
    }

    public void recordFailedLogin() {
        this.failedLoginAttempts++;
        if (this.failedLoginAttempts >= 5) {
            this.accountNonLocked = false; // Lock après 5 échecs
        }
    }

    public void addUserRole(UserRole userRole) {
        userRoles.add(userRole);
        userRole.setUser(this);
    }

    // Getters/Setters
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setAccountNonLocked(boolean accountNonLocked) { this.accountNonLocked = accountNonLocked; }
    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
    public Set<UserRole> getUserRoles() { return userRoles; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
}